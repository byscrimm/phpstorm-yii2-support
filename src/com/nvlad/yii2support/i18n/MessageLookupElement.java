package com.nvlad.yii2support.i18n;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NVlad on 06.01.2017.
 */
class MessageLookupElement extends LookupElement {
    final private PhpPsiElement myElement;
    final private ArrayHashElement myMessage;

    MessageLookupElement(PhpPsiElement element, ArrayHashElement message) {
        myElement = element;
        myMessage = message;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return Util.PhpExpressionValue((PhpExpression) myMessage.getKey());
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);

        if (myMessage.getKey() instanceof StringLiteralExpression) {
            presentation.setItemText(((StringLiteralExpression) myMessage.getKey()).getContents());
            presentation.setIcon(myMessage.getKey().getIcon(0));
        }

        PhpExpression value = (PhpExpression) myMessage.getValue();
        if (value != null) {
            String text = Util.PhpExpressionValue(value);

            if (!text.isEmpty()) {
                presentation.setTailText(" = " + text, true);
            }

            presentation.setTypeText(value.getType().toString());
            presentation.setTypeGrayed(true);
        }
    }

    @Override
    public AutoCompletionPolicy getAutoCompletionPolicy() {
        return AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE;
    }

}
