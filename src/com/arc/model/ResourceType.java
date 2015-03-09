package com.arc.model;

import java.io.File;

/**
 * 资源类型（抽象类）
 * @author xiaofei9
 * 2015-1-30 下午6:57:46
 */

public abstract class ResourceType {
    private final String mType;

    public ResourceType(final String type) {
        super();
        mType = type;
    }

    public String getType() {
        return mType;
    }

    public abstract boolean doesFileDeclareResource(File parent, String fileName, String fileContents, String resourceName);

    public boolean doesFileUseResource(final File parent, final String fileName, final String fileContents, final String resourceName) {
        return false;
    }
}
