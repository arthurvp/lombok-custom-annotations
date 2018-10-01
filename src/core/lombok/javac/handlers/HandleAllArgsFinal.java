/*
 * Copyright (C) 2012-2016 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.AllArgsFinal;
import lombok.experimental.NonFinal;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;

/**
 * Handles the {@code lombok.AllArgsFinal} annotation for javac.
 */
@ProviderFor(JavacASTVisitor.class)
@HandlerPriority(-2048)
public class HandleAllArgsFinal extends JavacASTAdapter {
    public boolean generateAllArgsFinal(JavacNode typeNode, JavacNode errorNode) {
        JCClassDecl typeDecl = null;
        if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
        long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
        boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;

        if (typeDecl == null || notAClass) {
            errorNode.addError("@AllArgsFinal is only supported on a class or an enum.");
            return false;
        }

        for (JavacNode method : typeNode.down()) {
            if (method.getKind() != Kind.METHOD) continue;

            setAllArgsFinal(method);
        }

        return true;
    }

    public void setAllArgsFinal(JavacNode method) {
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) method.get();
        for (JCTree.JCVariableDecl param : methodDecl.getParameters()) {
            if ((param.mods.flags & Flags.FINAL) == 0) {
                if (!hasAnnotationAndDeleteIfNeccessary(NonFinal.class, method.getNodeFor(param))) {
                    param.mods.flags |= Flags.FINAL;
                }
            }
        }
        method.rebuild();
    }

    @Override public void visitType(JavacNode typeNode, JCClassDecl type) {
        AnnotationValues<AllArgsFinal> argsFinal = null;
        JavacNode source = typeNode;

        AllArgsFinal af = null;
        for (JavacNode jn : typeNode.down()) {
            if (jn.getKind() != Kind.ANNOTATION) continue;
            JCAnnotation ann = (JCAnnotation) jn.get();
            JCTree typeTree = ann.annotationType;
            if (typeTree == null) continue;
            String typeTreeToString = typeTree.toString();
            if (!typeTreeToString.equals("AllArgsFinal") && !typeTreeToString.equals("lombok.experimental.AllArgsFinal")) continue;
            if (!typeMatches(AllArgsFinal.class, jn, typeTree)) continue;

            source = jn;
            argsFinal = createAnnotation(AllArgsFinal.class, jn);

            handleExperimentalFlagUsage(jn, ConfigurationKeys.ALL_ARGS_FINAL_FLAG_USAGE, "@AllArgsFinal");

            af = argsFinal.getInstance();

            deleteAnnotationIfNeccessary(jn, AllArgsFinal.class);
            deleteImportFromCompilationUnit(jn, "lombok.AccessLevel");
            break;
        }

        if (af == null && (type.mods.flags & (Flags.INTERFACE | Flags.ANNOTATION)) != 0) return;

        if (argsFinal == null) return;

        generateAllArgsFinal(typeNode, source);
    }
}
