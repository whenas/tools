package security.des;

import org.apache.commons.codec.binary.Hex;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;

/**
 * 加密密钥和解密密钥相同
 * 对称加密算法DES(数据加密标准)
 */
public class DESTest {






    /**
     * 加密
     */
    public static byte[] encrypt(byte[] content,byte[]keyBytes){
        try {
            DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = factory.generateSecret(desKeySpec);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,secretKey,new IvParameterSpec(desKeySpec.getKey()));
            byte[] bytes = cipher.doFinal(content);
            return bytes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    /**
     * 解密
     */
    public static byte[] decrypt(byte[] content,byte[]keyBytes){
        String result = null;
        try {
            DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = factory.generateSecret(desKeySpec);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE,secretKey,new IvParameterSpec(keyBytes));
            byte[] bytes = cipher.doFinal(content);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }





    public static void main(String[] args) {
        String content = "hello word";
        String key = "01234567";
        byte[]result = encrypt(content.getBytes(),key.getBytes());//加密
        decrypt(result,key.getBytes());

    }
}
