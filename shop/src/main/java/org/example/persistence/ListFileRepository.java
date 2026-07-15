package org.example.persistence;

import org.example.exception.PersistenceException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

abstract class ListFileRepository<T extends Serializable> {

    private final Path filePath;

    protected ListFileRepository(Path filePath) {
        this.filePath = filePath;
    }

    public void save(List<T> items) {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(filePath))) {
                out.writeObject(new ArrayList<>(items));
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to save data to " + filePath, e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> load() {
        if (!Files.exists(filePath)) {
            return List.of();
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(filePath))) {
            return (List<T>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenceException("Failed to load data from " + filePath, e);
        }
    }
}
