package dev.overwave.server;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/map")
public class MinecraftMap {
    //    D:\Users\Rodion\Downloads\MinedMap-1.16.Win64\MinedMap-1.16.Win64\viewer
    @RequestMapping(value = "{realm}/**")
    public ResponseEntity<byte[]> getMap(@PathVariable("realm") String realm, HttpServletRequest request) throws IOException {
        String name = ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).substring(8);

        Path file;
        if (realm.equals("nor")) {
            file = Paths.get("D:\\Users\\Rodion\\Downloads\\MinedMap-1.16.Win64\\MinedMap-1.16.Win64\\viewer\\", name);
        } else {
            file = Paths.get("D:\\Users\\Rodion\\Downloads\\MinedMap-1.16.Win64\\MinedMap-1.16.Win64\\viewer_end\\", name);
        }

        String extension = name.substring(name.lastIndexOf('.') + 1);

        HttpHeaders headers = new HttpHeaders();
        if (Files.exists(file)) {
            switch (extension) {
                case "html" -> headers.setContentType(MediaType.TEXT_HTML);
                case "png" -> headers.setContentType(MediaType.IMAGE_PNG);
                case "css" -> headers.setContentType(new MediaType("text", "css"));
                case "js" -> headers.setContentType(new MediaType("text", "javascript"));
                case "json" -> headers.setContentType(MediaType.APPLICATION_JSON);
                default -> headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            return new ResponseEntity<>(IOUtils.toByteArray(Files.newInputStream(file)), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
