package com.data.stateDescription;

import java.lang.StringBuilder;

public class Description {
    private final String name;
    private final String file;
    private final String text;
    public Description(String stateName, String sourceFile, String description) {
        name = stateName;
        file = sourceFile;
        text = description;
    }
    public String getName() {
        return name;
    }
    public String getSourceFile() {
        return file;
    }
    public String getDescription() {
        return text;
    }
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Description))
            return false;
        Description d = (Description)o;
        return d.name.equals(name) &&
               d.file.equals(file) &&
               d.text.equals(text);
    }
    @Override public int hashCode() {
        // Computed as recommended by Effective Java Second Edition
        int result = 17;
        result = 31 * result + name.hashCode();
        result = 31 * result + file.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" (");
        sb.append(file);
        sb.append("): ");
        sb.append(text);
        return sb.toString();
    }
}
