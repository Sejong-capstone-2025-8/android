package com.toprunner.imagestory.data.database;

import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenDelegate;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.SQLite;
import androidx.sqlite.SQLiteConnection;
import com.toprunner.imagestory.data.dao.FairyTaleDao;
import com.toprunner.imagestory.data.dao.FairyTaleDao_Impl;
import com.toprunner.imagestory.data.dao.ImageDao;
import com.toprunner.imagestory.data.dao.ImageDao_Impl;
import com.toprunner.imagestory.data.dao.MusicDao;
import com.toprunner.imagestory.data.dao.MusicDao_Impl;
import com.toprunner.imagestory.data.dao.TextDao;
import com.toprunner.imagestory.data.dao.TextDao_Impl;
import com.toprunner.imagestory.data.dao.VoiceDao;
import com.toprunner.imagestory.data.dao.VoiceDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile FairyTaleDao _fairyTaleDao;

  private volatile VoiceDao _voiceDao;

  private volatile ImageDao _imageDao;

  private volatile TextDao _textDao;

  private volatile MusicDao _musicDao;

  @Override
  @NonNull
  protected RoomOpenDelegate createOpenDelegate() {
    final RoomOpenDelegate _openDelegate = new RoomOpenDelegate(1, "0c24d59f97883a43571d120f4f117110", "b724facf540c3a464e9b0b34aa7405a7") {
      @Override
      public void createAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `fairy_tales` (`fairy_tales_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `voice_id` INTEGER NOT NULL, `image_id` INTEGER NOT NULL, `text_id` INTEGER NOT NULL, `music_id` INTEGER NOT NULL, `attribute` TEXT NOT NULL, `created_at` INTEGER NOT NULL)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `voices` (`voice_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `voice_path` TEXT NOT NULL, `attribute` TEXT NOT NULL, `created_at` INTEGER NOT NULL)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `images` (`image_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `image_path` TEXT NOT NULL, `created_at` INTEGER NOT NULL)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `texts` (`text_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text_path` TEXT NOT NULL, `created_at` INTEGER NOT NULL)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `musics` (`music_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `music_path` TEXT NOT NULL, `attribute` TEXT NOT NULL, `created_at` INTEGER NOT NULL)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        SQLite.execSQL(connection, "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0c24d59f97883a43571d120f4f117110')");
      }

      @Override
      public void dropAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `fairy_tales`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `voices`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `images`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `texts`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `musics`");
      }

      @Override
      public void onCreate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      public void onOpen(@NonNull final SQLiteConnection connection) {
        internalInitInvalidationTracker(connection);
      }

      @Override
      public void onPreMigrate(@NonNull final SQLiteConnection connection) {
        DBUtil.dropFtsSyncTriggers(connection);
      }

      @Override
      public void onPostMigrate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      @NonNull
      public RoomOpenDelegate.ValidationResult onValidateSchema(
          @NonNull final SQLiteConnection connection) {
        final Map<String, TableInfo.Column> _columnsFairyTales = new HashMap<String, TableInfo.Column>(8);
        _columnsFairyTales.put("fairy_tales_id", new TableInfo.Column("fairy_tales_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFairyTales.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFairyTales.put("voice_id", new TableInfo.Column("voice_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFairyTales.put("image_id", new TableInfo.Column("image_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFairyTales.put("text_id", new TableInfo.Column("text_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFairyTales.put("music_id", new TableInfo.Column("music_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFairyTales.put("attribute", new TableInfo.Column("attribute", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFairyTales.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysFairyTales = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesFairyTales = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFairyTales = new TableInfo("fairy_tales", _columnsFairyTales, _foreignKeysFairyTales, _indicesFairyTales);
        final TableInfo _existingFairyTales = TableInfo.read(connection, "fairy_tales");
        if (!_infoFairyTales.equals(_existingFairyTales)) {
          return new RoomOpenDelegate.ValidationResult(false, "fairy_tales(com.toprunner.imagestory.data.entity.FairyTaleEntity).\n"
                  + " Expected:\n" + _infoFairyTales + "\n"
                  + " Found:\n" + _existingFairyTales);
        }
        final Map<String, TableInfo.Column> _columnsVoices = new HashMap<String, TableInfo.Column>(5);
        _columnsVoices.put("voice_id", new TableInfo.Column("voice_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVoices.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVoices.put("voice_path", new TableInfo.Column("voice_path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVoices.put("attribute", new TableInfo.Column("attribute", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVoices.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysVoices = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesVoices = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVoices = new TableInfo("voices", _columnsVoices, _foreignKeysVoices, _indicesVoices);
        final TableInfo _existingVoices = TableInfo.read(connection, "voices");
        if (!_infoVoices.equals(_existingVoices)) {
          return new RoomOpenDelegate.ValidationResult(false, "voices(com.toprunner.imagestory.data.entity.VoiceEntity).\n"
                  + " Expected:\n" + _infoVoices + "\n"
                  + " Found:\n" + _existingVoices);
        }
        final Map<String, TableInfo.Column> _columnsImages = new HashMap<String, TableInfo.Column>(4);
        _columnsImages.put("image_id", new TableInfo.Column("image_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("image_path", new TableInfo.Column("image_path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysImages = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesImages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoImages = new TableInfo("images", _columnsImages, _foreignKeysImages, _indicesImages);
        final TableInfo _existingImages = TableInfo.read(connection, "images");
        if (!_infoImages.equals(_existingImages)) {
          return new RoomOpenDelegate.ValidationResult(false, "images(com.toprunner.imagestory.data.entity.ImageEntity).\n"
                  + " Expected:\n" + _infoImages + "\n"
                  + " Found:\n" + _existingImages);
        }
        final Map<String, TableInfo.Column> _columnsTexts = new HashMap<String, TableInfo.Column>(3);
        _columnsTexts.put("text_id", new TableInfo.Column("text_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTexts.put("text_path", new TableInfo.Column("text_path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTexts.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysTexts = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesTexts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTexts = new TableInfo("texts", _columnsTexts, _foreignKeysTexts, _indicesTexts);
        final TableInfo _existingTexts = TableInfo.read(connection, "texts");
        if (!_infoTexts.equals(_existingTexts)) {
          return new RoomOpenDelegate.ValidationResult(false, "texts(com.toprunner.imagestory.data.entity.TextEntity).\n"
                  + " Expected:\n" + _infoTexts + "\n"
                  + " Found:\n" + _existingTexts);
        }
        final Map<String, TableInfo.Column> _columnsMusics = new HashMap<String, TableInfo.Column>(5);
        _columnsMusics.put("music_id", new TableInfo.Column("music_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMusics.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMusics.put("music_path", new TableInfo.Column("music_path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMusics.put("attribute", new TableInfo.Column("attribute", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMusics.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysMusics = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesMusics = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMusics = new TableInfo("musics", _columnsMusics, _foreignKeysMusics, _indicesMusics);
        final TableInfo _existingMusics = TableInfo.read(connection, "musics");
        if (!_infoMusics.equals(_existingMusics)) {
          return new RoomOpenDelegate.ValidationResult(false, "musics(com.toprunner.imagestory.data.entity.MusicEntity).\n"
                  + " Expected:\n" + _infoMusics + "\n"
                  + " Found:\n" + _existingMusics);
        }
        return new RoomOpenDelegate.ValidationResult(true, null);
      }
    };
    return _openDelegate;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final Map<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final Map<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "fairy_tales", "voices", "images", "texts", "musics");
  }

  @Override
  public void clearAllTables() {
    super.performClear(false, "fairy_tales", "voices", "images", "texts", "musics");
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final Map<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(FairyTaleDao.class, FairyTaleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VoiceDao.class, VoiceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ImageDao.class, ImageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TextDao.class, TextDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MusicDao.class, MusicDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final Set<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public FairyTaleDao fairyTaleDao() {
    if (_fairyTaleDao != null) {
      return _fairyTaleDao;
    } else {
      synchronized(this) {
        if(_fairyTaleDao == null) {
          _fairyTaleDao = new FairyTaleDao_Impl(this);
        }
        return _fairyTaleDao;
      }
    }
  }

  @Override
  public VoiceDao voiceDao() {
    if (_voiceDao != null) {
      return _voiceDao;
    } else {
      synchronized(this) {
        if(_voiceDao == null) {
          _voiceDao = new VoiceDao_Impl(this);
        }
        return _voiceDao;
      }
    }
  }

  @Override
  public ImageDao imageDao() {
    if (_imageDao != null) {
      return _imageDao;
    } else {
      synchronized(this) {
        if(_imageDao == null) {
          _imageDao = new ImageDao_Impl(this);
        }
        return _imageDao;
      }
    }
  }

  @Override
  public TextDao textDao() {
    if (_textDao != null) {
      return _textDao;
    } else {
      synchronized(this) {
        if(_textDao == null) {
          _textDao = new TextDao_Impl(this);
        }
        return _textDao;
      }
    }
  }

  @Override
  public MusicDao musicDao() {
    if (_musicDao != null) {
      return _musicDao;
    } else {
      synchronized(this) {
        if(_musicDao == null) {
          _musicDao = new MusicDao_Impl(this);
        }
        return _musicDao;
      }
    }
  }
}
