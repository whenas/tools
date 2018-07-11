package security;

import sun.misc.BASE64Encoder;

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
    public String encryptMD5(String message){
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
     * @param args
     */




    public static void main(String[] args) {
        MessageDigestAlgorithm mda = new MessageDigestAlgorithm();
        String str = mda.encryptMD5(message);
        System.out.println(str);

    }


}
