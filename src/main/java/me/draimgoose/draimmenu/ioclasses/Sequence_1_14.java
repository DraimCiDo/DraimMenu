package me.draimgoose.draimmenu.ioclasses;

//1.14+ Imports
import me.draimgoose.draimmenu.DraimMenu;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class Sequence_1_14 {
    DraimMenu plugin;
    public Sequence_1_14(DraimMenu pl) {
        this.plugin = pl;
    }

    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        //это считывает зашифрованные файлы ресурсов в файле jar
        byte[] buffer = IOUtils.toByteArray(initialStream);
        return new CharSequenceReader(new String(buffer));
    }
}
