package org.deppwang.litespring.v4.annotation;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.SpringAsmInfo;
import org.springframework.asm.Type;
import org.springframework.util.ClassUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AnnotationMetadata extends ClassVisitor {
    // 存放类注解，有序的链表
    private final Set<String> annotationSet = new LinkedHashSet<>(4);
    // 存放当前类所有注解，类注解+属性注解
    private final Map<String, LinkedHashMap> attributeMap = new LinkedHashMap(4);

    private String className;

    public AnnotationMetadata() {
        super(SpringAsmInfo.ASM_VERSION);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {

        String className = Type.getType(desc).getClassName();
        this.annotationSet.add(className);
        return new AnnotationAttributesReadingVisitor(className, this.attributeMap);
    }

    public void visit(int version, int access, String name, String signature, String supername, String[] interfaces) {
        this.className = ClassUtils.convertResourcePathToClassName(name);
    }

    public String getClassName() {
        return this.className;
    }

    public boolean hasAnnotation(String annotationType) {
        return this.annotationSet.contains(annotationType);
    }

    public LinkedHashMap getAnnotationAttributes(String annotationType) {
        return this.attributeMap.get(annotationType);
    }


}

final class AnnotationAttributesReadingVisitor extends AnnotationVisitor {

    private final String annotationType;

    private final Map<String, LinkedHashMap> attributesMap;

    LinkedHashMap<String, Object> attributes = new LinkedHashMap<String, Object>();

    public AnnotationAttributesReadingVisitor(
            String annotationType, Map<String, LinkedHashMap> attributesMap) {
        super(SpringAsmInfo.ASM_VERSION);

        this.annotationType = annotationType;
        this.attributesMap = attributesMap;
    }

    @Override
    public final void visitEnd() {
        this.attributesMap.put(this.annotationType, this.attributes);
    }

    public void visit(String attributeName, Object attributeValue) {
        this.attributes.put(attributeName, attributeValue);
    }

}