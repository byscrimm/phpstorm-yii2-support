package io.github.byscrimm.yii2insight.validation.entities;

import com.jetbrains.php.lang.psi.elements.PhpNamedElement;

public class Validator {
    public String alias;

    public PhpNamedElement validator;

    public Validator(PhpNamedElement element) {
        alias = null;
        validator = element;
    }

    public Validator(String alias, PhpNamedElement element) {
        this.alias = alias;
        validator = element;
    }
}
