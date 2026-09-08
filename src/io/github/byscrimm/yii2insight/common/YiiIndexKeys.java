package io.github.byscrimm.yii2insight.common;

import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubIndexKey;

public interface YiiIndexKeys {
    StubIndexKey<String, PsiFile> VIEW = StubIndexKey.createIndexKey("io.github.byscrimm.yii2insight.view.index");
}
