package org.absorb.net.data.nbt;

import me.nullicorn.nedit.NBTInputStream;
import me.nullicorn.nedit.NBTOutputStream;
import me.nullicorn.nedit.exception.NBTParseException;
import me.nullicorn.nedit.type.NBTCompound;
import org.absorb.net.data.NetEntryData;
import org.absorb.net.data.Serializer;
import org.absorb.utils.MarkedByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class NBTTagCompoundGroupSerializer implements Serializer<NBTCompound> {
    @Override
    public NetEntryData<NBTCompound> read(int position, ByteBuffer bytes) {
        byte[] bytesArray = bytes.array();
        bytesArray = Arrays.copyOfRange(bytesArray, position, bytesArray.length);
        MarkedByteArrayInputStream markedIS = new MarkedByteArrayInputStream(bytesArray);
        NBTInputStream is = new NBTInputStream(markedIS);
        NBTCompound compound;
        try {
            compound = is.readFully();
        } catch (NBTParseException e) {
            try {
                int tagId = is.read();
                throw new IllegalStateException("Cannot read TagType with ID of " + tagId, e);
            } catch (IOException ex) {
                throw new IllegalStateException(e);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return new NetEntryData<>(position, (int) (position + markedIS.getCounter()), compound);
    }

    @Override
    public ByteBuffer write(NBTCompound value) {
        try {
            ByteArrayOutputStream baOs = new ByteArrayOutputStream();
            new NBTOutputStream(baOs, false).writeCompound(value, true);
            return ByteBuffer.wrap(baOs.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
