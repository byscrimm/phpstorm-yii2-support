package com.nvlad.yii2support.migrations.commands;

import java.util.function.Consumer;

/** Process listeners receive arbitrary chunks, not complete lines. */
public final class OutputLines {
    private final StringBuilder pending = new StringBuilder();

    public synchronized void accept(String chunk, Consumer<String> consumer) {
        pending.append(chunk);
        int end;
        while ((end = pending.indexOf("\n")) >= 0) {
            String line = pending.substring(0, end);
            pending.delete(0, end + 1);
            consumer.accept(line.endsWith("\r") ? line.substring(0, line.length() - 1) : line);
        }
        // Bound memory even for commands emitting long progress records without newlines.
        if (pending.length() > 65536) flush(consumer);
    }

    public synchronized void flush(Consumer<String> consumer) {
        if (!pending.isEmpty()) { consumer.accept(pending.toString()); pending.setLength(0); }
    }
}
