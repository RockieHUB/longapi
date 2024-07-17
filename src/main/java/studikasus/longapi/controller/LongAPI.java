package studikasus.longapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@CrossOrigin("*")
public class LongAPI {

    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
    private final Map<String, CompletableFuture<String>> prosesMap = new HashMap<>();

    @GetMapping("/proses-lama")
    public ResponseEntity<String> prosesLama(@RequestParam String namaUser) {
        String prosesId = UUID.randomUUID().toString();
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Proses dengan ID " + prosesId + " Milik " + namaUser + " selesai";
        }, executor);
        prosesMap.put(prosesId, future);
        // Cek apakah antrian thread pool penuh
        if (executor.getQueue().size() > 0) {
            return ResponseEntity.ok("Permintaan anda masuk ke antrian tunggu proses dengan ID: " + prosesId);
        } else {
            return ResponseEntity.ok("Permintaan anda sedang di proses dengan ID: " + prosesId);
        }
    }

    @GetMapping("/status-proses/{prosesId}")
    public ResponseEntity<String> statusProses(@PathVariable String prosesId) {
        CompletableFuture<String> future = prosesMap.get(prosesId);
        if (future == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proses tidak ditemukan");
        }
        if (future.isDone()) {
            return ResponseEntity.ok(future.join());
        } else {
            boolean isRunning = Thread.getAllStackTraces().keySet().stream()
                .anyMatch(thread -> thread.getName().contains(future.toString()));

            if (isRunning) {
                return ResponseEntity.ok("Proses sedang berjalan...");
            } else {
                return ResponseEntity.ok("Proses sedang menunggu di antrian...");
            }
        }
    }
    
}
