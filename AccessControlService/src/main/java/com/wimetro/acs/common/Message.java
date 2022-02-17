package com.wimetro.acs.common;

import com.wimetro.acs.util.ProtocolFiledUtil.FieldParser;
import com.wimetro.acs.util.ProtocolFiledUtil.AcsCmdPropParam;
import com.wimetro.acs.util.ProtocolFiledUtil.CmdProp;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @title: Message
 * @author: Ellie
 * @date: 2022/02/10 11:28
 * @description:
 **/
@Data
@Slf4j
public abstract class Message<T extends MessageBody> {

    private static final String MSG_SPLITTER = ";";
    private static final int MSG_LENGTH_FIELD_LENGTH = 4;

    private MessageHeader messageHeader;
    private T messageBody;

    public T getMessageBody(){
        return messageBody;
    }

    public void encode(ByteBuf byteBuf) {
        // 报文头
        String msgTypeStr = String.format("%04d", messageHeader.getMsgType());
        String msgHeadStr = msgTypeStr + ";";

        // 报文体编码
        Class cls = messageBody.getClass();
        List<AcsCmdPropParam> sParams = getSortedParams(cls);
        String bodyStr = encodeMsgBody(sParams);

        // 长度计算
        int length = msgHeadStr.length() + bodyStr.length() + MSG_LENGTH_FIELD_LENGTH + 1;
        String lengthStr = String.format("%04d", length) + ";";

        // 报文组装
        byteBuf.writeBytes(msgHeadStr.getBytes(StandardCharsets.UTF_8));
        byteBuf.writeBytes(lengthStr.getBytes(StandardCharsets.UTF_8));
        if (StringUtils.hasText(bodyStr)) {
            byteBuf.writeBytes(bodyStr.getBytes(StandardCharsets.UTF_8));
        }

        log.info("[encode]{}", byteBuf.toString(StandardCharsets.UTF_8));
    }

    private String encodeMsgBody(List<AcsCmdPropParam> sParams) {
        StringBuilder retSb = new StringBuilder();
        for (AcsCmdPropParam sParam : sParams) {
            Field field = sParam.getField();
            field.setAccessible(true);
            String fieldVal = null;
            try {
                Object fieldValObj = field.get(messageBody);
                String codecMethod = sParam.getCodec();

                fieldVal = (String)codec(fieldValObj, codecMethod);

            } catch (IllegalAccessException e) {
                log.error("属性{}访问异常，消息编码失败", field.getName(), e);
                return null;
            }

            if (StringUtils.hasText(fieldVal)) {
                retSb.append(fieldVal).append(MSG_SPLITTER);
            }
        }

        return retSb.toString();
    }

    public abstract Class<T> getMessageBodyDecodeClass(int opcode);

    public void decode(ByteBuf msg) {
        ByteBuf msgTypeBuf = msg.readBytes(4);
        msg.skipBytes(1);
        ByteBuf msgLengthBuf = msg.readBytes(4);
        msg.skipBytes(1);

        String msgTypeStr = msgTypeBuf.toString(CharsetUtil.UTF_8);
        int msgType = Integer.parseInt(msgTypeStr);

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setMsgType(msgType);
        this.messageHeader = messageHeader;

        // 消息体解析
        String msgBodyStr = msg.toString(Charset.forName("UTF-8"));
        T body = decodeMsgBody(msgType, msgBodyStr);

//        Constructor constructor = bodyClazz.getConstructor(String.class);
//        T body = (T)constructor.newInstance(msgBodyStr);
        this.messageBody = body;

        log.info("[decode]type={},length={},{}",
                messageHeader.getMsgType(),
                msgLengthBuf.toString(StandardCharsets.UTF_8),
                body.toString());
    }

    private T decodeMsgBody(int msgType, String msgBodyStr) {
        Class<T> bodyClazz = getMessageBodyDecodeClass(msgType);
        List<Field> allFields = getAllFields(bodyClazz);
        T body = null;
        try {
            body = bodyClazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("创建消息对象失败, e");
        }

        // 根据字段长度判断是否需要对协议进行具体解码
        List<String> fieldVals;
        if (allFields.size() > 1) {
            fieldVals = new ArrayList<>(Arrays.asList(msgBodyStr.split(MSG_SPLITTER)));
        } else {
            fieldVals = new ArrayList<>();
            fieldVals.add(msgBodyStr);
        }

        for (Field field : allFields) {
            CmdProp cmdProp = field.getAnnotation(CmdProp.class);
            if (cmdProp == null) {
                continue;
            }

            int idx = cmdProp.index() - 1;
            if (allFields.size() <= idx) {
                log.error("消息体参数个数小于{}，解析失败", idx);
                return null;
            } else {
                field.setAccessible(true);
                try {
                    String fieldValStr = fieldVals.get(idx);
                    String codecMethod = cmdProp.deCodec();
                    Object val = StringUtils.hasText(fieldValStr) ? null : fieldValStr;

                    // 字段解码
                    val = codec(fieldValStr, codecMethod);

                    field.set(body, val);
                } catch (IllegalAccessException e) {
                    log.error("设置字段值失败: {}={}", field.getName(), fieldVals.get(idx));
                    return null;
                }
            }
        }

        return body;
    }


    private static List<Field> getAllFields(Class cls) {
        Field[] fields = cls.getDeclaredFields();
        List<Field> allFields = new ArrayList<>(Arrays.asList(fields));
        return allFields;
    }

    public static List<AcsCmdPropParam> getSortedParams(Class cls) {
        List<Field> allFields = getAllFields(cls);
        List<AcsCmdPropParam> sParams = new ArrayList<>();
        for (Field field : allFields) {
            CmdProp cmdProp = field.getAnnotation(CmdProp.class);
            if (cmdProp != null) {
                sParams.add(new AcsCmdPropParam(cmdProp.index(), cmdProp.enCodec(), field));
            }
        }
        Collections.sort(sParams);
        return sParams;
    }

    private Object codec(Object fieldVal, String codecMethod) {
        Object val;

        // 字符串，无需转换
        if (AcsGatewayConsts.ENCODER_TO_STR.equals(codecMethod)) {
            val = fieldVal.toString();
            return val;
        }

        // 根据编码方法进行转换
        try {
            Method method = FieldParser.class.getDeclaredMethod(codecMethod, String.class);
            val = method.invoke(null, fieldVal);
        } catch (NoSuchMethodException e) {
            log.error("未定义解码方法：{}，消息解析失败", codecMethod, e);
            return null;
        } catch (InvocationTargetException e) {
            log.error("调用解码方法{}发生异常，消息解析失败", codecMethod, e);
            return null;
        } catch (IllegalAccessException e) {
            log.error("设置字段值失败");
            return null;
        }

        return val;
    }
}
