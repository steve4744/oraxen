package io.th0rgal.oraxen.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class VirtualFile implements Comparable<VirtualFile> {

    private String parentFolder;
    private String name;
    private InputStream inputStream;

    public VirtualFile(String parentFolder, String name, InputStream inputStream) {
        this.parentFolder = OS.getOs().getName().startsWith("Windows")
                ? parentFolder.replace("\\", "/")
                : parentFolder;
        this.name = name;
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getPath() {
        return parentFolder.isEmpty()
                ? name
                : parentFolder + "/" + name;
    }

    public void setPath(String newPath) {
        String newParent = newPath.substring(0, newPath.lastIndexOf("/"));
        this.parentFolder = OS.getOs().getName().startsWith("Windows")
                ? newParent.replace("\\", "/")
                : newParent;
        this.name = newPath.substring(newPath.lastIndexOf("/") + 1);
    }

    @Override
    public int compareTo(@NotNull VirtualFile other) {
        return other.getPath().compareTo(getPath());
    }

    public JsonElement toJsonElement() {
        InputStream fontInput = inputStream;
        String fontContent;
        try {
            fontContent = IOUtils.toString(fontInput, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Logs.logError(Utils.removeParentDirs(getPath()) + " was empty");
            return null;
        }
        return JsonParser.parseString(fontContent);
    }

}
