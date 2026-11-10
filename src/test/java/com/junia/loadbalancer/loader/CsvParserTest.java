package com.junia.loadbalancer.loader;

import com.junia.loadbalancer.exception.InvalidDataException;
import com.junia.loadbalancer.model.FileUploadRequest;
import com.junia.loadbalancer.model.HttpRequest;
import com.junia.loadbalancer.model.Request;
import com.junia.loadbalancer.model.Server;
import com.junia.loadbalancer.model.ServerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private ServerCsvParser serverParser = new ServerCsvParser();
    private RequestCsvParser requestParser = new RequestCsvParser();

    @Test
    void shouldParseValidServerLine() throws InvalidDataException {
        Server server = serverParser.parseLine("web-01,20,3,UP");

        assertEquals("web-01", server.getName());
        assertEquals(20, server.getCapacity());
        assertEquals(3, server.getWeight());
        assertEquals(ServerStatus.UP, server.getStatus());
    }

    @Test
    void shouldCreateRightRequestTypeFromLine() throws InvalidDataException {
        Request http = requestParser.parseLine("R1,HTTP,2026-11-15T09:00:00,");
        Request upload = requestParser.parseLine("R2,UPLOAD,2026-11-15T09:00:10,25");

        assertInstanceOf(HttpRequest.class, http);
        assertInstanceOf(FileUploadRequest.class, upload);
        assertEquals(4, upload.getLoad());
    }

    @Test
    void shouldThrowInvalidDataExceptionForBadLines() {
        assertThrows(InvalidDataException.class, () -> serverParser.parseLine("web-01,abc,1,UP"));
        assertThrows(InvalidDataException.class, () -> serverParser.parseLine("web-01,10,1,MAYBE"));
        assertThrows(InvalidDataException.class, () -> serverParser.parseLine("web-01,10"));
        assertThrows(InvalidDataException.class, () -> requestParser.parseLine("R1,PING,2026-11-15T09:00:00,"));
        assertThrows(InvalidDataException.class, () -> requestParser.parseLine("R1,HTTP,hier,"));
        assertThrows(InvalidDataException.class, () -> requestParser.parseLine("R1,STREAM,2026-11-15T09:00:00,-5"));
    }

    @Test
    void shouldSkipInvalidLinesAndKeepValidOnes(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("servers.csv");
        Files.writeString(file, """
                name,capacity,weight,status
                web-01,20,3,UP
                web-02,abc,2,UP

                web-03,10,1,DOWN
                """);

        List<Server> servers = serverParser.parse(file);

        assertEquals(2, servers.size());
        assertEquals("web-03", servers.get(1).getName());
    }

    @Test
    void shouldThrowIOExceptionWhenFileDoesNotExist(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("missing.csv");

        assertThrows(IOException.class, () -> serverParser.parse(missing));
    }
}
