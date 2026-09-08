package io.github.byscrimm.yii2insight.views.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import io.github.byscrimm.yii2insight.utils.Yii2InsightSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 28.12.2016.
 */
class ViewLookupElement extends LookupElement {
    final private PsiFile myFile;
    final private String myName;
    final private String myTail;

    ViewLookupElement(PsiFile psiFile, String insertText) {
        myFile = psiFile;
        VirtualFile file = psiFile.getVirtualFile();

        myName = insertText;
        final String ext = file.getExtension();
        final String defaultViewExtension = Yii2InsightSettings.getInstance(myFile.getProject()).defaultViewExtension;
        if (ext != null && ext.equals(defaultViewExtension) && !insertText.endsWith("." + defaultViewExtension)) {
            myTail = "." + file.getExtension();
        } else {
            myTail = null;
        }
    }

    @NotNull
    @Override
    public String getLookupString() {
        return myName;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setIcon(myFile.getIcon(0));
        presentation.setItemText(myName);
        presentation.setItemTextBold(true);
        if (myTail != null) {
            presentation.setTailText(myTail, true);
        }
        presentation.setTypeText("View");
        presentation.setTypeGrayed(true);
    }
}
