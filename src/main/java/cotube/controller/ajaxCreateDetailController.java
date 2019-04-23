package cotube.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64.Decoder;

@Controller
@RequestMapping(value = "/createComicDetail.html")
public class ajaxCreateDetailController {



    @RequestMapping(value = "/saveComic", method = RequestMethod.POST)
    public RedirectView saveComic(HttpServletRequest request) throws IOException {
        String username = request.getParameter("username");
        String img = request.getParameter("data");
        byte[] imageByte;
        BufferedImage image = null;
        Decoder decoder = java.util.Base64.getMimeDecoder();
        Integer comicId = 23333; //comidId which need to return

        imageByte = decoder.decode(img);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
        image = ImageIO.read(bis);
        bis.close();
        
        File outputfile = new File("cotubeImage.png"); //file path and file name need to change
        ImageIO.write(image, "png", outputfile);
        System.out.println(request.getParameter("username"));

        /*

            Create a new comic, regularComic and panel in the db with the username given
            Fill the other fields of the tables with placeholder
            Save the newly generated comicId in the variable comidId

        */

        return new RedirectView("?createComicId="+Integer.toString(comicId));
    }

 


}
