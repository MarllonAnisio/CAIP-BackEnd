package org.marllon.caip.domains.image.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.marllon.caip.domains.image.exeptions.FileStorageException;
import org.marllon.caip.domains.image.exeptions.ImageDeleteException;
import org.marllon.caip.domains.image.exeptions.ImageUploadException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementação do FileStorageService usando a plataforma Cloudinary.
 * Esta é a implementação "real" que será usada em produção.
 */
@Service
@ConditionalOnMissingBean(FileStorageService.class)
@RequiredArgsConstructor
public class CloudinaryService implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file, String folderName) { // Remove "throws IOException"
        try {
            var options = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "auto"
            );
            var result = cloudinary.uploader().upload(file.getBytes(), options);
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new ImageUploadException("Falha ao fazer upload da imagem para o Cloudinary", e);
        }
    }

    @Override
    public void delete(String url){
        try {
            String publicId = extractPublicIdFromUrl(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            // Você pode criar uma ImageDeleteException se quiser ser mais específico
            throw new ImageDeleteException("Falha ao deletar a imagem do Cloudinary: " + url, e);
        }
    }


    /**
     * Extrai o Public ID de uma URL do Cloudinary.
     * Ex: "https://.../v123/caip/reports/imagem.jpg" -> "caip/reports/imagem"
     */
    private String extractPublicIdFromUrl(String url) {
        // Regex para capturar o caminho após a versão (ex: v123456/) até a extensão do arquivo.
        Pattern pattern = Pattern.compile("upload/v\\d+/(.*?)(?:\\.[^.]*)?$");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Se não encontrar o padrão, pode ser uma URL inválida ou formatada de forma diferente.
        throw new IllegalArgumentException("URL do Cloudinary inválida ou não contém um Public ID reconhecível.");
    }
}
