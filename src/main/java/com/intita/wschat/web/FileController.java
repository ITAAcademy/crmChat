package com.intita.wschat.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	public void saveFile(MultipartHttpServletRequest request,HttpServletResponse response,Principal principal) {
	 
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
         String orgName=null;
	     try {
	                //just temporary save file info into ufile
	    	 orgName = mpf.getOriginalFilename();
             String filePath = realPathtoUploads + orgName;
             File dest = new File(filePath);
             mpf.transferTo(dest);
	 
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	  
	    }
	     String downloadLink = String.format("download_file?ownerId=%1$s&fileName=%2$s",principal.getName(),orgName);
	     response.setStatus(HttpServletResponse.SC_OK);
	     response.setContentLength(downloadLink.length());
	     response.setContentType("text/plain");
	     response.setCharacterEncoding("UTF-8");
	     try {
			response.getWriter().write(downloadLink);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     log.info("downloadLink:"+downloadLink);
	     // return downloadLink;
	    //////
	}
	  /**
     * Size of a byte buffer to read/write file
     */
    private static final int BUFFER_SIZE = 4096;
             
    /**
     * Path of the file to be downloaded, relative to application's directory
     */
     
    /**
     * Method for handling file download request from client
     */
    @RequestMapping(method = RequestMethod.GET, value="/download_file")
    public void doDownload(HttpServletRequest request,
            HttpServletResponse response,@RequestParam Long ownerId, @RequestParam String fileName) throws IOException {
 
        // get absolute path of the application
        ServletContext context = request.getServletContext();
       // String appPath = context.getRealPath("");
 
        // construct the complete absolute path of the file
        String fullPath = uploadDir +"\\"+ownerId+"\\"+ fileName;      
        File downloadFile = new File(fullPath);
        if (!downloadFile.exists()){
        	log.debug("No such file:"+fullPath);
        	return;
        }
        FileInputStream inputStream = new FileInputStream(downloadFile);
         
        // get MIME type of the file
        String mimeType = context.getMimeType(fullPath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);
 
        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());
 
        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                downloadFile.getName());
        response.setHeader(headerKey, headerValue);
 
        // get output stream of the response
        OutputStream outStream = response.getOutputStream();
 
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
 
        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
 
        inputStream.close();
        outStream.close();
 
    }
}
