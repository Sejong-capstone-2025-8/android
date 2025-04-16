package com.toprunner.imagestory.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteConnectionUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.toprunner.imagestory.data.entity.VoiceEntity;
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
public final class VoiceDao_Impl implements VoiceDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<VoiceEntity> __insertAdapterOfVoiceEntity;

  private final EntityDeleteOrUpdateAdapter<VoiceEntity> __updateAdapterOfVoiceEntity;

  public VoiceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfVoiceEntity = new EntityInsertAdapter<VoiceEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `voices` (`voice_id`,`title`,`voice_path`,`attribute`,`created_at`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final VoiceEntity entity) {
        statement.bindLong(1, entity.getVoice_id());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        if (entity.getVoice_path() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getVoice_path());
        }
        if (entity.getAttribute() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getAttribute());
        }
        statement.bindLong(5, entity.getCreated_at());
      }
    };
    this.__updateAdapterOfVoiceEntity = new EntityDeleteOrUpdateAdapter<VoiceEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `voices` SET `voice_id` = ?,`title` = ?,`voice_path` = ?,`attribute` = ?,`created_at` = ? WHERE `voice_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final VoiceEntity entity) {
        statement.bindLong(1, entity.getVoice_id());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        if (entity.getVoice_path() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getVoice_path());
        }
        if (entity.getAttribute() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getAttribute());
        }
        statement.bindLong(5, entity.getCreated_at());
        statement.bindLong(6, entity.getVoice_id());
      }
    };
  }

  @Override
  public Object insertVoice(final VoiceEntity voiceEntity,
      final Continuation<? super Long> $completion) {
    if (voiceEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfVoiceEntity.insertAndReturnId(_connection, voiceEntity);
    }, $completion);
  }

  @Override
  public Object updateVoice(final VoiceEntity voiceEntity,
      final Continuation<? super Integer> $completion) {
    if (voiceEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      int _result = 0;
      _result += __updateAdapterOfVoiceEntity.handle(_connection, voiceEntity);
      return _result;
    }, $completion);
  }

  @Override
  public Object getVoiceById(final long voiceId,
      final Continuation<? super VoiceEntity> $completion) {
    final String _sql = "SELECT * FROM voices WHERE voice_id = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, voiceId);
        final int _columnIndexOfVoiceId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "voice_id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfVoicePath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "voice_path");
        final int _columnIndexOfAttribute = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "attribute");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final VoiceEntity _result;
        if (_stmt.step()) {
          final long _tmpVoice_id;
          _tmpVoice_id = _stmt.getLong(_columnIndexOfVoiceId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpVoice_path;
          if (_stmt.isNull(_columnIndexOfVoicePath)) {
            _tmpVoice_path = null;
          } else {
            _tmpVoice_path = _stmt.getText(_columnIndexOfVoicePath);
          }
          final String _tmpAttribute;
          if (_stmt.isNull(_columnIndexOfAttribute)) {
            _tmpAttribute = null;
          } else {
            _tmpAttribute = _stmt.getText(_columnIndexOfAttribute);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _result = new VoiceEntity(_tmpVoice_id,_tmpTitle,_tmpVoice_path,_tmpAttribute,_tmpCreated_at);
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
  public Object getAllVoices(final Continuation<? super List<VoiceEntity>> $completion) {
    final String _sql = "SELECT * FROM voices";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfVoiceId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "voice_id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfVoicePath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "voice_path");
        final int _columnIndexOfAttribute = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "attribute");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final List<VoiceEntity> _result = new ArrayList<VoiceEntity>();
        while (_stmt.step()) {
          final VoiceEntity _item;
          final long _tmpVoice_id;
          _tmpVoice_id = _stmt.getLong(_columnIndexOfVoiceId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpVoice_path;
          if (_stmt.isNull(_columnIndexOfVoicePath)) {
            _tmpVoice_path = null;
          } else {
            _tmpVoice_path = _stmt.getText(_columnIndexOfVoicePath);
          }
          final String _tmpAttribute;
          if (_stmt.isNull(_columnIndexOfAttribute)) {
            _tmpAttribute = null;
          } else {
            _tmpAttribute = _stmt.getText(_columnIndexOfAttribute);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _item = new VoiceEntity(_tmpVoice_id,_tmpTitle,_tmpVoice_path,_tmpAttribute,_tmpCreated_at);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteVoice(final long voiceId, final Continuation<? super Integer> $completion) {
    final String _sql = "DELETE FROM voices WHERE voice_id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, voiceId);
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
