package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.model.SystemSetting;
import fj.ac.usp.spacehub.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class SettingsService {
    private final SystemSettingRepository repo;

    public String get(String key, String def) { return repo.findById(key).map(SystemSetting::getValue).orElse(def); }
    public int getInt(String key, int def) { try { return Integer.parseInt(get(key, "")); } catch (Exception e) { return def; } }
    public double getDouble(String key, double def) { try { return Double.parseDouble(get(key, "")); } catch (Exception e) { return def; } }
    public boolean getBoolean(String key, boolean def) { return Boolean.parseBoolean(get(key, String.valueOf(def))); }
    public LocalTime getTime(String key, LocalTime def) { try { return LocalTime.parse(get(key, "")); } catch (Exception e) { return def; } }

    public List<SystemSetting> all() {
        return repo.findAll().stream().sorted(Comparator.comparing(SystemSetting::getKey)).toList();
    }

    @Transactional
    public void update(String key, String value) {
        SystemSetting s = repo.findById(key).orElseThrow();
        s.setValue(value.trim());
    }
}