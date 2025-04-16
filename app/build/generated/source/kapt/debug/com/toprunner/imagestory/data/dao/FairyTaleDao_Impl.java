package com.toprunner.imagestory.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteConnectionUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.toprunner.imagestory.data.entity.FairyTaleEntity;
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
public final class FairyTaleDao_Impl implements FairyTaleDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<FairyTaleEntity> __insertAdapterOfFairyTaleEntity;

  private final EntityDeleteOrUpdateAdapter<FairyTaleEntity> __updateAdapterOfFairyTaleEntity;

  public FairyTaleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfFairyTaleEntity = new EntityInsertAdapter<FairyTaleEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `fairy_tales` (`fairy_tales_id`,`title`,`voice_id`,`image_id`,`text_id`,`music_id`,`attribute`,`created_at`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final FairyTaleEntity entity) {
        statement.bindLong(1, entity.getFairy_tales_id());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        statement.bindLong(3, entity.getVoice_id());
        statement.bindLong(4, entity.getImage_id());
        statement.bindLong(5, entity.getText_id());
        statement.bindLong(6, entity.getMusic_id());
        if (entity.getAttribute() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getAttribute());
        }
        statement.bindLong(8, entity.getCreated_at());
      }
    };
    this.__updateAdapterOfFairyTaleEntity = new EntityDeleteOrUpdateAdapter<FairyTaleEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `fairy_tales` SET `fairy_tales_id` = ?,`title` = ?,`voice_id` = ?,`image_id` = ?,`text_id` = ?,`music_id` = ?,`attribute` = ?,`created_at` = ? WHERE `fairy_tales_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final FairyTaleEntity entity) {
        statement.bindLong(1, entity.getFairy_tales_id());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        statement.bindLong(3, entity.getVoice_id());
        statement.bindLong(4, entity.getImage_id());
        statement.bindLong(5, entity.getText_id());
        statement.bindLong(6, entity.getMusic_id());
        if (entity.getAttribute() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getAttribute());
        }
        statement.bindLong(8, entity.getCreated_at());
        statement.bindLong(9, entity.getFairy_tales_id());
      }
    };
  }

  @Override
  public Object insertFairyTale(final FairyTaleEntity fairyTaleEntity,
      final Continuation<? super Long> $completion) {
    if (fairyTaleEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfFairyTaleEntity.insertAndReturnId(_connection, fairyTaleEntity);
    }, $completion);
  }

  @Override
  public Object updateFairyTale(final FairyTaleEntity fairyTaleEntity,
      final Continuation<? super Integer> $completion) {
    if (fairyTaleEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      int _result = 0;
      _result += __updateAdapterOfFairyTaleEntity.handle(_connection, fairyTaleEntity);
      return _result;
    }, $completion);
  }

  @Override
  public Object getFairyTaleById(final long fairyTaleId,
      final Continuation<? super FairyTaleEntity> $completion) {
    final String _sql = "SELECT * FROM fairy_tales WHERE fairy_tales_id = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, fairyTaleId);
        final int _columnIndexOfFairyTalesId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "fairy_tales_id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfVoiceId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "voice_id");
        final int _columnIndexOfImageId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "image_id");
        final int _columnIndexOfTextId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "text_id");
        final int _columnIndexOfMusicId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "music_id");
        final int _columnIndexOfAttribute = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "attribute");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final FairyTaleEntity _result;
        if (_stmt.step()) {
          final long _tmpFairy_tales_id;
          _tmpFairy_tales_id = _stmt.getLong(_columnIndexOfFairyTalesId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final long _tmpVoice_id;
          _tmpVoice_id = _stmt.getLong(_columnIndexOfVoiceId);
          final long _tmpImage_id;
          _tmpImage_id = _stmt.getLong(_columnIndexOfImageId);
          final long _tmpText_id;
          _tmpText_id = _stmt.getLong(_columnIndexOfTextId);
          final long _tmpMusic_id;
          _tmpMusic_id = _stmt.getLong(_columnIndexOfMusicId);
          final String _tmpAttribute;
          if (_stmt.isNull(_columnIndexOfAttribute)) {
            _tmpAttribute = null;
          } else {
            _tmpAttribute = _stmt.getText(_columnIndexOfAttribute);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _result = new FairyTaleEntity(_tmpFairy_tales_id,_tmpTitle,_tmpVoice_id,_tmpImage_id,_tmpText_id,_tmpMusic_id,_tmpAttribute,_tmpCreated_at);
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
  public Object getAllFairyTales(final Continuation<? super List<FairyTaleEntity>> $completion) {
    final String _sql = "SELECT * FROM fairy_tales ORDER BY created_at DESC";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfFairyTalesId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "fairy_tales_id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfVoiceId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "voice_id");
        final int _columnIndexOfImageId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "image_id");
        final int _columnIndexOfTextId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "text_id");
        final int _columnIndexOfMusicId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "music_id");
        final int _columnIndexOfAttribute = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "attribute");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final List<FairyTaleEntity> _result = new ArrayList<FairyTaleEntity>();
        while (_stmt.step()) {
          final FairyTaleEntity _item;
          final long _tmpFairy_tales_id;
          _tmpFairy_tales_id = _stmt.getLong(_columnIndexOfFairyTalesId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final long _tmpVoice_id;
          _tmpVoice_id = _stmt.getLong(_columnIndexOfVoiceId);
          final long _tmpImage_id;
          _tmpImage_id = _stmt.getLong(_columnIndexOfImageId);
          final long _tmpText_id;
          _tmpText_id = _stmt.getLong(_columnIndexOfTextId);
          final long _tmpMusic_id;
          _tmpMusic_id = _stmt.getLong(_columnIndexOfMusicId);
          final String _tmpAttribute;
          if (_stmt.isNull(_columnIndexOfAttribute)) {
            _tmpAttribute = null;
          } else {
            _tmpAttribute = _stmt.getText(_columnIndexOfAttribute);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _item = new FairyTaleEntity(_tmpFairy_tales_id,_tmpTitle,_tmpVoice_id,_tmpImage_id,_tmpText_id,_tmpMusic_id,_tmpAttribute,_tmpCreated_at);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteFairyTale(final long fairyTaleId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "DELETE FROM fairy_tales WHERE fairy_tales_id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, fairyTaleId);
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
