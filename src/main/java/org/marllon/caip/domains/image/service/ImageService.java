package org.marllon.caip.domains.image.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final Cloudinary cloudinary;

    public String upload(MultipartFile file) throws IOException {
        var options = ObjectUtils.asMap("folder", "caip/reports");
        var result = cloudinary.uploader().upload(file.getBytes(), options);
        return result.get("secure_url").toString();
    }
}

