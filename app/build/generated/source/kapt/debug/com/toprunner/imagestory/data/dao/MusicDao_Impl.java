package com.toprunner.imagestory.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteConnectionUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.toprunner.imagestory.data.entity.MusicEntity;
import java.lang.Class;
import java.lang.Integer;
import java.lang.Long;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class MusicDao_Impl implements MusicDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<MusicEntity> __insertAdapterOfMusicEntity;

  private final EntityDeleteOrUpdateAdapter<MusicEntity> __updateAdapterOfMusicEntity;

  public MusicDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfMusicEntity = new EntityInsertAdapter<MusicEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `musics` (`music_id`,`title`,`music_path`,`attribute`,`created_at`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final MusicEntity entity) {
        statement.bindLong(1, entity.getMusic_id());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        if (entity.getMusic_path() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getMusic_path());
        }
        if (entity.getAttribute() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getAttribute());
        }
        statement.bindLong(5, entity.getCreated_at());
      }
    };
    this.__updateAdapterOfMusicEntity = new EntityDeleteOrUpdateAdapter<MusicEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `musics` SET `music_id` = ?,`title` = ?,`music_path` = ?,`attribute` = ?,`created_at` = ? WHERE `music_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final MusicEntity entity) {
        statement.bindLong(1, entity.getMusic_id());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        if (entity.getMusic_path() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getMusic_path());
        }
        if (entity.getAttribute() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getAttribute());
        }
        statement.bindLong(5, entity.getCreated_at());
        statement.bindLong(6, entity.getMusic_id());
      }
    };
  }

  @Override
  public Object insertMusic(final MusicEntity musicEntity,
      final Continuation<? super Long> $completion) {
    if (musicEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfMusicEntity.insertAndReturnId(_connection, musicEntity);
    }, $completion);
  }

  @Override
  public Object updateMusic(final MusicEntity musicEntity,
      final Continuation<? super Integer> $completion) {
    if (musicEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      int _result = 0;
      _result += __updateAdapterOfMusicEntity.handle(_connection, musicEntity);
      return _result;
    }, $completion);
  }

  @Override
  public Object getMusicById(final long musicId,
      final Continuation<? super MusicEntity> $completion) {
    final String _sql = "SELECT * FROM musics WHERE music_id = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, musicId);
        final int _columnIndexOfMusicId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "music_id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfMusicPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "music_path");
        final int _columnIndexOfAttribute = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "attribute");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final MusicEntity _result;
        if (_stmt.step()) {
          final long _tmpMusic_id;
          _tmpMusic_id = _stmt.getLong(_columnIndexOfMusicId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpMusic_path;
          if (_stmt.isNull(_columnIndexOfMusicPath)) {
            _tmpMusic_path = null;
          } else {
            _tmpMusic_path = _stmt.getText(_columnIndexOfMusicPath);
          }
          final String _tmpAttribute;
          if (_stmt.isNull(_columnIndexOfAttribute)) {
            _tmpAttribute = null;
          } else {
            _tmpAttribute = _stmt.getText(_columnIndexOfAttribute);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _result = new MusicEntity(_tmpMusic_id,_tmpTitle,_tmpMusic_path,_tmpAttribute,_tmpCreated_at);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object getAllMusic(final Continuation<? super List<MusicEntity>> $completion) {
    final String _sql = "SELECT * FROM musics";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfMusicId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "music_id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfMusicPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "music_path");
        final int _columnIndexOfAttribute = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "attribute");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final List<MusicEntity> _result = new ArrayList<MusicEntity>();
        while (_stmt.step()) {
          final MusicEntity _item;
          final long _tmpMusic_id;
          _tmpMusic_id = _stmt.getLong(_columnIndexOfMusicId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpMusic_path;
          if (_stmt.isNull(_columnIndexOfMusicPath)) {
            _tmpMusic_path = null;
          } else {
            _tmpMusic_path = _stmt.getText(_columnIndexOfMusicPath);
          }
          final String _tmpAttribute;
          if (_stmt.isNull(_columnIndexOfAttribute)) {
            _tmpAttribute = null;
          } else {
            _tmpAttribute = _stmt.getText(_columnIndexOfAttribute);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _item = new MusicEntity(_tmpMusic_id,_tmpTitle,_tmpMusic_path,_tmpAttribute,_tmpCreated_at);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object getMusicByGenre(final String genre,
      final Continuation<? super List<MusicEntity>> $completion) {
    final String _sql = "SELECT * FROM musics WHERE attribute = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (genre == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, genre);
        }
        final int _columnIndexOfMusicId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "music_id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfMusicPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "music_path");
        final int _columnIndexOfAttribute = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "attribute");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final List<MusicEntity> _result = new ArrayList<MusicEntity>();
        while (_stmt.step()) {
          final MusicEntity _item;
          final long _tmpMusic_id;
          _tmpMusic_id = _stmt.getLong(_columnIndexOfMusicId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpMusic_path;
          if (_stmt.isNull(_columnIndexOfMusicPath)) {
            _tmpMusic_path = null;
          } else {
            _tmpMusic_path = _stmt.getText(_columnIndexOfMusicPath);
          }
          final String _tmpAttribute;
          if (_stmt.isNull(_columnIndexOfAttribute)) {
            _tmpAttribute = null;
          } else {
            _tmpAttribute = _stmt.getText(_columnIndexOfAttribute);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _item = new MusicEntity(_tmpMusic_id,_tmpTitle,_tmpMusic_path,_tmpAttribute,_tmpCreated_at);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteMusic(final long musicId, final Continuation<? super Integer> $completion) {
    final String _sql = "DELETE FROM musics WHERE music_id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, musicId);
        _stmt.step();
        return SQLiteConnectionUtil.getTotalChangedRows(_connection);
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
