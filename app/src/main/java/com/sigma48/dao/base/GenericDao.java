package com.sigma48.dao.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class GenericDao<T> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private final TypeReference<List<T>> typeReference;

    public GenericDao(String filePath, TypeReference<List<T>> typeReference) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.typeReference = typeReference;
        initializeFile();
    }

    private void initializeFile() {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
                objectMapper.writeValue(new File(filePath), new ArrayList<>()); // Tulis array JSON kosong
            }
        } catch (IOException e) {
            throw new RuntimeException("Gagal menginisialisasi file data: " + filePath, e);
        }
    }

    public List<T> getAll() {
        try {
            File file = new File(filePath);
            if (file.length() == 0) {
                return new ArrayList<>(); // Kembalikan list kosong jika file kosong
            }
            return objectMapper.readValue(file, typeReference);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    protected void saveAll(List<T> entities) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), entities);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<T> find(Predicate<T> predicate) {
        return getAll().stream().filter(predicate).findFirst();
    }

    public void save(T entity, Predicate<T> identityPredicate) {
        List<T> entities = new ArrayList<>(getAll());
        // Hapus entitas lama jika ada
        entities.removeIf(identityPredicate);
        // Tambahkan entitas baru atau yang diperbarui
        entities.add(entity);
        saveAll(entities);
    }
    
    public boolean delete(Predicate<T> predicate) {
        List<T> entities = new ArrayList<>(getAll());
        int initialSize = entities.size();
        entities = entities.stream()
                           .filter(predicate.negate())
                           .collect(Collectors.toList());
        
        if (entities.size() < initialSize) {
            saveAll(entities);
            return true;
        }
        return false;
    }
}