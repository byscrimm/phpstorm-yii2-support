package io.github.byscrimm.yii2insight.objectfactory;

import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oleg on 14.03.2017.
 */
public class ObjectFactoryReferenceProvider extends com.intellij.psi.PsiReferenceProvider {
    @NotNull
    @Override
    public ObjectFactoryReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        if (!(psiElement instanceof com.jetbrains.php.lang.psi.elements.StringLiteralExpression)
                || io.github.byscrimm.yii2insight.common.PhpArrays.keyEntry(psiElement) == null) return new ObjectFactoryReference[0];
        List<ObjectFactoryReference> references = new ArrayList<>();

        ObjectFactoryReference reference = new ObjectFactoryReference(psiElement);
        references.add(reference);


        return references.toArray(new ObjectFactoryReference[0]);
    }
}
