package com.h2bros.processing;

import com.google.auto.service.AutoService;
import com.h2bros.annotation.BindView;
import com.h2bros.annotation.OnClick;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
public class GProcessor extends AbstractProcessor {
    private static final String SUFFIX = "_GBinding";
    private static final String CONST_PARAM_TARGET_NAME = "target";
    private static final String CONST_PARAM_VIEW_BINDING_NAME = "rootView";
    private static final String ANDROID_VIEW_TYPE = "android.view.View";
    private static final String VIEW_BINDING_STATEMENT_FORMAT =
            "target.%1s = androidx.viewbinding.ViewBindings.findChildViewById(rootView, %2s)";

    private static final String VIEW_ONCLICK_STATEMENT_FORMAT =
            "androidx.viewbinding.ViewBindings.findChildViewById(rootView, %1s).setOnClickListener(new View.OnClickListener() {\n" +
                    "            @Override\n" +
                    "            public void onClick(View view) {\n" +
                    "                target.%2s(%3s);\n" +
                    "            }\n" +
                    "        });";
    private Filer mFiler;
    private Types mTypes;
    private Elements mElements;
    private final Map<String, List<Element>> mListViewMap = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mTypes = processingEnvironment.getTypeUtils();
        mElements = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        annotations.add(OnClick.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            String ANDROID_VIEW_TYPE = "android.view.View";
            if (mTypes.isSubtype(element.asType(),
                    mElements.getTypeElement(ANDROID_VIEW_TYPE).asType())) {
                Symbol.ClassSymbol typeElement = (Symbol.ClassSymbol) element.getEnclosingElement();
                String key = typeElement.flatName().toString();
                if (mListViewMap.get(key) == null) {
                    mListViewMap.put(key, new ArrayList<>());
                }
                mListViewMap.get(key).add(element);
            }
        }

        for (Element element : roundEnvironment.getElementsAnnotatedWith(OnClick.class)) {
            if (element instanceof ExecutableElement) {
                Symbol.ClassSymbol typeElement = (Symbol.ClassSymbol) element.getEnclosingElement();
                String key = typeElement.flatName().toString();
                if (mListViewMap.get(key) == null) {
                    mListViewMap.put(key, new ArrayList<>());
                }
                mListViewMap.get(key).add(element);
            }
        }

        if (mListViewMap.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, List<Element>> entry : mListViewMap.entrySet()) {
            MethodSpec constructor = createConstructorBinding(entry.getValue());
            TypeSpec binder = createClass(getClassName(entry.getKey()), constructor);
            JavaFile javaFile = JavaFile.builder(getPackage(entry.getValue().get(0)), binder).build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private TypeSpec createClass(String className, MethodSpec constructor) {
        return TypeSpec.classBuilder(className + SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .build();
    }

    private String getClassName(String qualifier) {
        String DOT = ".";
        return qualifier.substring(qualifier.lastIndexOf(DOT) + 1);
    }

    private String getPackage(Element element) {
        return mElements.getPackageOf(element).toString();
    }

    private MethodSpec createConstructorBinding(List<Element> elements) {
        Element firstElement = elements.get(0);
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(firstElement.getEnclosingElement().asType()),
                        CONST_PARAM_TARGET_NAME)
                .addParameter(TypeName.get(mElements.getTypeElement(ANDROID_VIEW_TYPE).asType()),
                        CONST_PARAM_VIEW_BINDING_NAME);
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element instanceof ExecutableElement) {
                for (int id : element.getAnnotation(OnClick.class).value()) {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    List<? extends VariableElement> params = executableElement.getParameters();
                    String param = "";
                    if (!params.isEmpty()) {
                        param = "(" + TypeName.get(params.get(0).asType()).toString() + ") view";
                    }
                    builder.addStatement(
                            String.format(VIEW_ONCLICK_STATEMENT_FORMAT,
                                    id,
                                    element.getSimpleName().toString(),
                                    param
                            ));
                }
            } else {
                builder.addStatement(
                        String.format(VIEW_BINDING_STATEMENT_FORMAT,
                                element.getSimpleName().toString(),
                                element.getAnnotation(BindView.class).value()));
            }
        }
        return builder.build();
    }
}
