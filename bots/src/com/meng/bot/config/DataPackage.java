package com.meng.bot.config;

import com.meng.tools.normal.BitConverter;
import com.meng.tools.normal.JSON;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DataPackage {

    protected final short headLength = 32;
    protected final short version = 1;
    public int dataPointer = 0;
    public BitConverter convert = BitConverter.getInstanceLittleEndian();

    public static final byte typeByte = 0;
    public static final byte typeShort = 1;
    public static final byte typeInt = 2;
    public static final byte typeLong = 3;
    public static final byte typeFloat = 4;
    public static final byte typeDouble = 5;
    public static final byte typeString = 6;
    public static final byte typeBoolean = 7;
    public static final byte typeFile = 8;


    public static final int opLoginQQ = 0;
    public static final int opLoginNick = 1;
    public static final int opPrivateMsg = 2;
    public static final int opGroupMsg = 3;
    public static final int opDiscussMsg = 4;
    public static final int opDeleteMsg = 5;
    public static final int opSendLike = 6;
    public static final int opCookies = 7;
    public static final int opCsrfToken = 8;
    public static final int opRecord = 9;
    public static final int opGroupKick = 10;
    public static final int opGroupBan = 11;
    public static final int opGroupAdmin = 12;
    public static final int opGroupWholeBan = 13;
    public static final int opGroupAnonymousBan = 14;
    public static final int opGroupAnonymous = 15;
    public static final int opGroupCard = 16;
    public static final int opGroupLeave = 17;
    public static final int opGroupSpecialTitle = 18;
    public static final int opGroupMemberInfo = 19;
    public static final int opDiscussLeave = 20;
    public static final int opFriendAddRequest = 21;
    public static final int opGroupMemberList = 22;
    public static final int opGroupList = 23;
    public static final int heardBeat = 24;

    public static final int onGroupMsg = 25;
    public static final int onPerSecMsgInfo=26;
    public static final int getConfig = 27;

    public static final int opAddQuestion = 28;
    public static final int opAllQuestion = 29;
    public static final int opSetQuestion = 30;
    public static final int opQuestionPic = 31;
    public static final int opTextNotify = 32;

    public static final int opEnableFunction=33;

    public static final int addGroup=34;
    public static final int addNotReplyUser = 35;
    public static final int addNotReplyWord = 36;
    public static final int addPersonInfo = 37;
    public static final int addMaster = 38;
    public static final int addAdmin = 39;
    public static final int addGroupAllow = 40 ;
    public static final int addBlackQQ = 41;
    public static final int addBlackGroup = 42;
    public static final int removeGroup = 43;
    public static final int removeNotReplyUser = 44;
    public static final int removeNotReplyWord = 45;
    public static final int removePersonInfo = 46;
    public static final int removeMaster = 47;
    public static final int removeAdmin = 48;
    public static final int removeGroupAllow = 49;
    public static final int removeBlackQQ = 50;
    public static final int removeBlackGroup = 51;
    public static final int setPersonInfo = 52;

    public static final int opGetApp = 53;
    public static final int opCrashLog = 54;
    public static final int opUploadApk = 55;

    public static final int verify = 56;
    

    public static DataPackageTx encode(long targetId, int opCode) {
        return new DataPackageTx(targetId, System.currentTimeMillis(), opCode);
    }

    public static DataPackageTx encode(DataPackage dataPack) {
        return new DataPackageTx(dataPack);
    }

    public static DataPackageRx decode(byte[] bytes) {
        return new DataPackageRx(bytes);
    }

    public byte[] getData() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public int getLength() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }  

    public short getHeadLength() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public short getVersion() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public long getPackageId() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public long getBotId() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public int getModuleId() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public int getOpCode() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public DataPackage write(byte b) {
        throw new UnsupportedOperationException("rx data package not support this.");
    }

    public DataPackage write(short s) {
        throw new UnsupportedOperationException("rx data package not support this.");
    }

    public DataPackage write(int i) {
        throw new UnsupportedOperationException("rx data package not support this.");
    }

    public DataPackage write(long l) {
        throw new UnsupportedOperationException("rx data package not support this.");
    }

    public DataPackage write(float f) {
        throw new UnsupportedOperationException("rx data package not support this.");
    }

    public DataPackage write(double d) {
        throw new UnsupportedOperationException("rx data package not support this.");
    }

    public DataPackage write(String s) {
        throw new UnsupportedOperationException("rx data package not support this.");
    }

    public DataPackage write(boolean b) {
        throw new UnsupportedOperationException("rx data package not support this.");
    }

    public DataPackage write(File file) {
        throw new UnsupportedOperationException("rx data package not support this.");
    }

    public File readFile(File file) {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public byte readByte() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public short readShort() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public int readInt() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public long readLong() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public float readFloat() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public double readDouble() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public String readString() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public boolean readBoolean() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public boolean hasNext() {
        throw new UnsupportedOperationException("tx data package not support this.");
    }

    public static class DataPackageTx extends DataPackage {

        private ByteArrayOutputStream data = new ByteArrayOutputStream();

        private DataPackageTx(long targetId, long timeStamp, int opCode) {
            //length(4) headLength(2) version(2) time(8) target/from(8) moduleId(4)
            writeByteDataIntoArray(convert.getBytes(0));
            writeByteDataIntoArray(convert.getBytes(headLength));
            writeByteDataIntoArray(convert.getBytes(version));
            writeByteDataIntoArray(convert.getBytes(timeStamp));
            writeByteDataIntoArray(convert.getBytes(targetId));
            writeByteDataIntoArray(convert.getBytes(0x9961));
            writeByteDataIntoArray(convert.getBytes(opCode));
        }   

        private DataPackageTx(DataPackage dataPack) {
            //length(4) headLength(2) version(2) time(8) target/from(8) moduleId(4)
            writeByteDataIntoArray(convert.getBytes(0));
            writeByteDataIntoArray(convert.getBytes(headLength));
            writeByteDataIntoArray(convert.getBytes(version));
            writeByteDataIntoArray(convert.getBytes(dataPack.getPackageId()));
            writeByteDataIntoArray(convert.getBytes(dataPack.getBotId()));
            writeByteDataIntoArray(convert.getBytes(0x9961));
            writeByteDataIntoArray(convert.getBytes(dataPack.getOpCode()));
        }

        private DataPackage writeByteDataIntoArray(byte... bs) {
            for (byte b:bs) {
                data.write(b);
                ++dataPointer;
            }
            return this;
        }

        @Override
        public byte[] getData() {
            byte[] retData = data.toByteArray();
            byte[] len = convert.getBytes(retData.length);
            retData[0] = len[0];
            retData[1] = len[1];
            retData[2] = len[2];
            retData[3] = len[3];
            return retData;
        }

        public DataPackage write(byte b) {
            writeByteDataIntoArray(typeByte);
            writeByteDataIntoArray(b);
            return this;
        }

        public DataPackage write(short s) {
            writeByteDataIntoArray(typeShort);
            writeByteDataIntoArray(convert.getBytes(s));
            return this;
        }

        public DataPackage write(int i) {
            writeByteDataIntoArray(typeInt);
            writeByteDataIntoArray(convert.getBytes(i));
            return this;
        }

        public DataPackage write(long l) {
            writeByteDataIntoArray(typeLong);
            writeByteDataIntoArray(convert.getBytes(l));
            return this;
        }

        public DataPackage write(float f) {
            writeByteDataIntoArray(typeFloat);
            writeByteDataIntoArray(convert.getBytes(f));
            return this;
        }

        public DataPackage write(double d) {
            writeByteDataIntoArray(typeDouble);
            writeByteDataIntoArray(convert.getBytes(d));
            return this;
        }

        public DataPackage write(String s) {
            writeByteDataIntoArray(typeString);
            byte[] stringBytes = convert.getBytes(s);
            write(stringBytes.length);
            writeByteDataIntoArray(stringBytes);
            return this;
        }

        public DataPackage write(boolean b) {
            writeByteDataIntoArray(typeBoolean);
            writeByteDataIntoArray((byte)(b ? 1: 0));
            return this;
        }

        public DataPackage write(File file) {
            try {
                FileInputStream fin = new FileInputStream(file);
                byte[] bs = new byte[(int)file.length()];
                fin.read(bs, 0, bs.length);
                writeByteDataIntoArray(typeFile);
                write(file.length());
                writeByteDataIntoArray(bs);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }
    }

    public static class DataPackageRx extends DataPackage {

        private byte[] data;

        private DataPackageRx(byte[] pack) {
            data = pack;
            dataPointer = headLength;
        }

        @Override
        public byte[] getData() {
            return data;
        }

        public int getLength() {
            return convert.toInt(data, 0);
        }  

        public short getHeadLength() {
            return convert.toShort(data, 4);
        }

        public short getVersion() {
            return convert.toShort(data, 6);
        }

        public long getPackageId() {
            return convert.toLong(data, 8);
        }

        public long getBotId() {
            return convert.toLong(data, 16);
        }

        public int getModuleId() {
            return convert.toInt(data, 24);
        }

        @Override
        public int getOpCode() {
            return convert.toInt(data, 28);
        }

        public File readFile(File file) {
            if (data[dataPointer++] == typeFile) {
                long fileLen = readLong();
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data, dataPointer, (int)fileLen);
                } catch (Exception e) {
                    file.delete();
                    file = null;
                }
                dataPointer += fileLen;
                return file;
            }
            throw new RuntimeException("not a file");
        }

        public byte readByte() {
            if (data[dataPointer++] == typeByte) {
                return data[dataPointer++];
            }
            throw new RuntimeException("not a byte number");
        }

        public short readShort() {
            if (data[dataPointer++] == typeShort) {
                short s = convert.toShort(data, dataPointer);
                dataPointer += 2;
                return s;
            }
            throw new RuntimeException("not a short number");
        }

        public int readInt() {
            if (data[dataPointer++] == typeInt) {
                int i= convert.toInt(data, dataPointer);
                dataPointer += 4;
                return i;
            }
            throw new RuntimeException("not a int number");
        }

        public long readLong() {
            if (data[dataPointer++] == typeLong) {
                long l= convert.toLong(data, dataPointer);
                dataPointer += 8;
                return l;
            }
            throw new RuntimeException("not a long number");
        }

        public float readFloat() {
            if (data[dataPointer++] == typeFloat) {
                float f = convert.toFloat(data, dataPointer);
                dataPointer += 4;
                return f;
            }
            throw new RuntimeException("not a float number");
        }

        public double readDouble() {
            if (data[dataPointer++] == typeDouble) {
                double d = convert.toDouble(data, dataPointer);
                dataPointer += 8;
                return d;
            }
            throw new RuntimeException("not a double number");
        }

        public String readString() {
            try {
                if (data[dataPointer++] == typeString) {
                    int len = readInt();
                    String s = convert.toString(data, dataPointer, len);
                    dataPointer += len;
                    return s;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
            return null;
        }

        public boolean readBoolean() {
            if (data[dataPointer++] == typeBoolean) {
                return data[dataPointer++] == 1;
            }
            throw new RuntimeException("not a boolean value");
        }

        public boolean hasNext() {
            return dataPointer != data.length;
        }
    }
}
