package security;

import org.apache.commons.codec.binary.Hex;
import sun.misc.BASE64Encoder;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 消息摘要算法
 * MD(Message Digest)
 * SHA(Secure Hash Algorithm)
 * MAC(Message Authentication Code)
 * 验证数据的完成性（不可逆加密）
 * 数字签名核心算法
 */
public class MessageDigestAlgorithm {

    private static String message = "hello word";

    /**
     * MD5（128位摘要信息）+BASE64(BASE64 严格地说，属于编码格式，而非加密算法)
     * apache DigestUtils也可实现
     */
    public String encryptMD5(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("Md5");
            byte[] md5Bytes = md.digest(message.getBytes());
            return new BASE64Encoder().encode(md5Bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * SHA
     * 安全散列算法
     * 固定长度摘要信息
     * SHA-1、SHA-2（SHA-224\SHA-256\SHA384\SHA-512）
     */
    public String encodeSHA256(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] sha = md.digest(message.getBytes());
            return new BASE64Encoder().encode(sha);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HMAC 含有密钥的散列函数算法
     * 融合MD、SHA
     * MD系列：HmacMD2\HmacMD4\HmacMD5
     * SHA系列:HmacSHA1\HmacSHA224\HmacSHA256\HmacSHA384\HmacSHA512
     * @param
     */
    public String hmacMD5(String message){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacMD5");//初始化keyGenerator
            SecretKey secretKey = keyGenerator.generateKey();//产生密钥
            byte[] key = secretKey.getEncoded();//获得密钥
            SecretKey restoreSecretKey = new SecretKeySpec(key,"HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(restoreSecretKey);//初始化mac
            byte[] resultBytes = mac.doFinal(message.getBytes());
            return Hex.encodeHexString(resultBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static void main(String[] args) {
        MessageDigestAlgorithm mda = new MessageDigestAlgorithm();
        String str = mda.encryptMD5(message);
        System.out.println(str);
        System.out.println(mda.encodeSHA256(message));
        System.out.println(mda.hmacMD5(message));


    }


}
