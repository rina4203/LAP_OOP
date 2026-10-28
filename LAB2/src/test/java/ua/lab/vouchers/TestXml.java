package ua.lab.vouchers;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class TestXml {

    public static final String VOUCHER = """
                <touristVoucher id="v01" type="EXCURSION">
                    <country>Італія</country>
                    <duration days="8" nights="7"/>
                    <transport>AIR</transport>
                    <hotel stars="4">
                        <meals>BB</meals>
                        <room>2</room>
                        <tv>true</tv>
                        <airConditioner>1</airConditioner>
                    </hotel>
                    <cost>
                        <service name="TRANSPORT">14500.00</service>
                        <service name="ACCOMMODATION">21000.00</service>
                    </cost>
                </touristVoucher>
            """;

    private TestXml() {
    }

    public static String document(String vouchers) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<touristVouchers>\n" + vouchers + "</touristVouchers>\n";
    }

    public static URL write(Path directory, String content) throws IOException {
        Path file = Files.createTempFile(directory, "vouchers", ".xml");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file.toUri().toURL();
    }

    public static URL resource(String name) {
        return Objects.requireNonNull(
                TestXml.class.getClassLoader().getResource(name), name);
    }
}
