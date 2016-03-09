package com.intita.wschat.web;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class FileController {

	private final static Logger log = LoggerFactory.getLogger(FileController.class);
	 @Value("${crmchat.upload_dir}")
	 private String uploadDir;
	@RequestMapping(method = RequestMethod.POST, value = "/upload_file")
	@ResponseBody
	public boolean saveFile(MultipartHttpServletRequest request,Principal principal) {
	 
		//0. notice, we have used MultipartHttpServletRequest
		 
	     //1. get the files from the request object
	     Iterator<String> itr =  request.getFileNames();
	 
	     MultipartFile mpf = request.getFile(itr.next());
	     System.out.println(mpf.getOriginalFilename() +" uploaded!");
         
         String realPathtoUploads =  uploadDir+"\\"+principal.getName()+"\\";
         File dir = new File(realPathtoUploads);
         boolean exists = dir.exists();
         if(!exists)
			dir.mkdirs();

	     try {
	                //just temporary save file info into ufile
	    	 String orgName = mpf.getOriginalFilename();
             String filePath = realPathtoUploads + orgName;
             File dest = new File(filePath);
             mpf.transferTo(dest);
	 
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	  
	    }
	      return true;
	    //////
	}
}
