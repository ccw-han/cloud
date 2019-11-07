package net.cyweb.config.ftp;

import cyweb.utils.ErrorCode;
import net.cyweb.model.Result;
import net.cyweb.model.UserProPs;
import net.cyweb.model.YangMember;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

@Configuration
public class FtpUtils {




    /**
     * Description: 向FTP服务器上传文件
     * @Version1.0
     * @param port FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param input 输入流
     * @param originFileName 文件原文件名
     * @param savePath 保存路径
     * @return 成功返回true，否则返回false
     */
    public  static Result  uploadFile(String host, int port, String username, String password, String originFileName, InputStream input, Result result,String savePath) {
        Result finalyResult=new Result();
        finalyResult.setCode(Result.Code.SUCCESS);
//        savePath="";
        YangMember yangMember=(YangMember) result.getData();
        //获取文件后缀名
        String[] a=originFileName.split("\\.");
        originFileName=UUID.randomUUID().toString()+"."+a[a.length-1];

        FTPClient ftp = new FTPClient();
        try {
            ftp.enterLocalPassiveMode();//被动模式
//            ftp.enterLocalActiveMode();//主动模式
            int reply;
            ftp.connect(host, port);//连接FTP服务器
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);//登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                finalyResult.setCode(Result.Code.ERROR);
                return finalyResult;
            }
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

//            ftp.makeDirectory(savePath+ yangMember.getMemberId());
//            ftp.changeWorkingDirectory(savePath+ yangMember.getMemberId());

            if(!ftp.changeWorkingDirectory(savePath+ yangMember.getMemberId())) {
                if (!ftp.makeDirectory(savePath+ yangMember.getMemberId())) {
                    throw new RuntimeException(savePath+ yangMember.getMemberId()+"，目录创建失败");
                }else{
                    ftp.changeWorkingDirectory(savePath+ yangMember.getMemberId());
                }
            }
            ftp.storeFile(originFileName,input);
            input.close();
            ftp.logout();
        } catch (Exception e) {
            e.printStackTrace();
            finalyResult.setCode(Result.Code.ERROR);
            finalyResult.setErrorCode(ErrorCode.ERROR_PIC_UPLOAD.getIndex());
            finalyResult.setMsg(ErrorCode.ERROR_PIC_UPLOAD.getMessage());
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
//        yangMember.setPic1(yangMember.getMemberId()+"/"+originFileName);
        finalyResult.setData(yangMember.getMemberId()+"/"+originFileName);
//        finalyResult.setData(yangMember);
        return finalyResult;
    }

    /**
     * Description: 向FTP服务器上传文件
     * @Version1.0
     * @return 成功返回true，否则返回false
     */
    public  static Result  uploadFileLocalFile(MultipartFile filea, Result result, String rootPath) {
        Result finalyResult=new Result();
        finalyResult.setCode(Result.Code.SUCCESS);
        String originFileName=filea.getOriginalFilename();

        YangMember yangMember=(YangMember) result.getData();
        //获取文件后缀名
        String[] a=originFileName.split("\\.");
        originFileName=UUID.randomUUID().toString()+"."+a[a.length-1];

        File file=new File(rootPath+yangMember.getMemberId()+File.separator+originFileName);
        File parentFile=file.getParentFile();

        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        try{
            filea.transferTo(file);
        }catch (Exception e){
            e.printStackTrace();
            finalyResult.setCode(Result.Code.ERROR);
            finalyResult.setErrorCode(ErrorCode.ERROR_PIC_UPLOAD.getIndex());
            finalyResult.setMsg(ErrorCode.ERROR_PIC_UPLOAD.getMessage());
        }

        finalyResult.setData(yangMember.getMemberId()+ "/"+originFileName);
        return finalyResult;
    }

    public static Result uploadFileFromBackSystem(MultipartFile filea,String rootPath,String path){
        Result result=new Result();
        String originFileName=filea.getOriginalFilename();
        String[] a=originFileName.split("\\.");
        originFileName=UUID.randomUUID().toString().replaceAll("-","")+"."+a[a.length-1];


        File file=new File(rootPath+path+File.separator+originFileName);
        File parentFile=file.getParentFile();
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        try{
            filea.transferTo(file);
            result.setCode(Result.Code.SUCCESS);
            result.setData(File.separator+path+File.separator+originFileName);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_PIC_UPLOAD.getIndex());
            result.setMsg(ErrorCode.ERROR_PIC_UPLOAD.getMessage());
        }
        return result;
    }

}
