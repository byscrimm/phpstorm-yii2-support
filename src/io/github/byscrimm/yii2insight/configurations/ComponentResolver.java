package io.github.byscrimm.yii2insight.configurations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import io.github.byscrimm.yii2insight.common.FileUtil;
import io.github.byscrimm.yii2insight.common.YiiContext;
import java.util.*;

public final class ComponentResolver {
    private ComponentResolver() {}

    public static Set<String> classes(Project project, VirtualFile origin, String name) {
        Set<String> values = new LinkedHashSet<>();
        int[] best = {-1};
        FileBasedIndex.getInstance().processValues(ComponentsIndex.identity, name, null, (file, value) -> {
            int priority = YiiContext.configPriority(origin, file, project.getBasePath());
            if (priority < 0 || priority < best[0]) return true;
            if (priority > best[0]) { values.clear(); best[0] = priority; }
            values.add(value);
            return true;
        }, GlobalSearchScope.projectScope(project));
        return values;
    }
    public static Set<String> classes(PsiElement origin, String name) {
        return classes(origin.getProject(), FileUtil.getVirtualFile(origin.getContainingFile()), name);
    }
}
