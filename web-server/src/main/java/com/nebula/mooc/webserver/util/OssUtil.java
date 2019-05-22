/*
 * @author Zhanghh
 * @date 2019/5/9
 */
package com.nebula.mooc.webserver.util;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;

@Component
public class OssUtil {

    private static final Logger logger = LoggerFactory.getLogger(OssUtil.class);

    @Autowired
    private OSSClient ossClient;

    @Value("${oss.headBucket}")
    private String headBucket;

    @Value("${oss.videoBucket}")
    private String videoBucket;

    /**
     * 传输过程监听
     */
    private class ProgressListenerImpl implements ProgressListener {

        private boolean success = false;
        private String fileName;

        ProgressListenerImpl(String key) {
            fileName = key;
        }

        @Override
        public void progressChanged(ProgressEvent progressEvent) {
            ProgressEventType eventType = progressEvent.getEventType();
            switch (eventType) {
                // 传输开始
                case TRANSFER_STARTED_EVENT:
                    logger.info("Upload start [{}].", fileName);
                    break;
                // 传输完成
                case TRANSFER_COMPLETED_EVENT:
                    success = true;
                    logger.info("Upload success [{}].", fileName);
                    break;
                // 传输失败
                case TRANSFER_FAILED_EVENT:
                    logger.info("Upload fail [{}].", fileName);
                    break;
            }
        }

        public boolean isSuccess() {
            return this.success;
        }

    }

    private boolean uploadFile(String key, MultipartFile file, String bucketName) {
        ProgressListenerImpl progressListener = new ProgressListenerImpl(key);
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream());
            putObjectRequest.setProgressListener(progressListener);
            ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return progressListener.isSuccess();
    }

    private void deleteFile(String key, String bucketName) {
        ossClient.deleteObject(bucketName, key);
    }

    public boolean uploadHead(String key, MultipartFile file) {
        return uploadFile(key, file, headBucket);
    }

    public boolean uploadVideo(String key, MultipartFile file) {
        return uploadFile(key, file, videoBucket);
    }

    public void deleteHead(String key) {
        deleteFile(key, headBucket);
    }

    public void deleteVideo(String key) {
        deleteFile(key, videoBucket);
    }

    @PreDestroy
    public void destroy() {
        ossClient.shutdown();
    }

}