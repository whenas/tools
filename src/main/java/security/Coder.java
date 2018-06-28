package security;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

/**
 * BASE64、MD5、SHA、HMAC几种方法
 * MD5、SHA、HMAC这三种加密算法，可谓是非可逆加密，就是不可解密的加密方法。
 * 我们通常只把他们作为加密的基础。单纯的以上三种的加密并不可靠。
 *  MD5、SHA以及HMAC是单向加密，任何数据加密后只会产生唯一的一个加密串，通常用来校验数据在传输过程中是否被修改。
 *  其中HMAC算法有一个密钥，增强了数据传输过程中的安全性，强化了算法外的不可控因素。
 * Created by lusongjiong on 2018/6/28.
 */
public class Coder {
    public static final String KEY_SHA = "SHA";
    public static final String KEY_MD5 = "MD5";
    /**
     * MAC算法可选以下多种算法
     *
     * <pre>
     * HmacMD5
     * HmacSHA1
     * HmacSHA256
     * HmacSHA384
     * HmacSHA512
     * </pre>
     */
    public static final String KEY_MAC = "HmacMD5";

    /**
     * BASE64加密
     * Base64内容传送编码被设计用来把任意序列的8位字节描述为一种不易被人直接识别的形式。
     * @param key
     * @return
     */
    public static String encryptBASE64(byte[] key){
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encodeBuffer(key);
    }

    /**
     * BASE64解密
     *
     * @param key
     * @return
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }

    /**
     * MD5加密(信息摘要算法)
     * @param data
     * @return
     */
    public static byte[] encryptMD5(byte[] data) throws Exception{
        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
        md5.update(data);
        return  md5.digest();
    }

    /**
     * SHA加密（安全散列算法）
     * 数字签名等密码学应用中重要的工具，被广泛地应用于电子商务等信息安全领域。 
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encryptSHA(byte[] data) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
        sha.update(data);
        return sha.digest();

    }

    /**
     * 初始化HMAC密钥
     *
     * @return
     * @throws Exception
     */
    public static String initMacKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC);
        SecretKey secretKey = keyGenerator.generateKey();
        return encryptBASE64(secretKey.getEncoded());
    }

    /**
     * HMAC加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(decryptBASE64(key), KEY_MAC);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);

        return mac.doFinal(data);

    }
























    public static void main(String[] args) throws Exception {
        //1.BASE64加解密
        String name = "zhangsan";
        String input = encryptBASE64(name.getBytes());
        System.out.println("加密后:"+input);
        byte[] output = decryptBASE64(input);
        String outputStr = new String(output);
        System.out.println("解密后:"+ outputStr);
    }

}
