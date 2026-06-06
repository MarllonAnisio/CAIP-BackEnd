package org.marllon.caip.domains.image.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface que define o contrato para um serviço de armazenamento de arquivos.
 * Qualquer implementação (Cloudinary, S3, Local Storage) deve seguir este contrato.
 *
 * As implementações não devem vazar exceções checadas como IOException,
 * mas sim envolvê-las em exceções de domínio não checadas (RuntimeException).
 */
public interface FileStorageService {

    /**
     * Realiza o upload de um arquivo.
     *
     * @param file O arquivo a ser enviado.
     * @param folderName O nome da pasta/diretório de destino no serviço de armazenamento.
     * @return A URL pública do arquivo após o upload.
     */
    String upload(MultipartFile file, String folderName);

    /**
     * Deleta um arquivo do serviço de armazenamento com base em sua URL pública.
     *
     * @param fileUrl A URL completa do arquivo a ser deletado.
     */
    void delete(String fileUrl);
}
