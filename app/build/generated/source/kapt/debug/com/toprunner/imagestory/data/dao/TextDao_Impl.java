package com.toprunner.imagestory.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteConnectionUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.toprunner.imagestory.data.entity.TextEntity;
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
public final class TextDao_Impl implements TextDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<TextEntity> __insertAdapterOfTextEntity;

  private final EntityDeleteOrUpdateAdapter<TextEntity> __updateAdapterOfTextEntity;

  public TextDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfTextEntity = new EntityInsertAdapter<TextEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `texts` (`text_id`,`text_path`,`created_at`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final TextEntity entity) {
        statement.bindLong(1, entity.getText_id());
        if (entity.getText_path() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getText_path());
        }
        statement.bindLong(3, entity.getCreated_at());
      }
    };
    this.__updateAdapterOfTextEntity = new EntityDeleteOrUpdateAdapter<TextEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `texts` SET `text_id` = ?,`text_path` = ?,`created_at` = ? WHERE `text_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final TextEntity entity) {
        statement.bindLong(1, entity.getText_id());
        if (entity.getText_path() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getText_path());
        }
        statement.bindLong(3, entity.getCreated_at());
        statement.bindLong(4, entity.getText_id());
      }
    };
  }

  @Override
  public Object insertText(final TextEntity textEntity,
      final Continuation<? super Long> $completion) {
    if (textEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfTextEntity.insertAndReturnId(_connection, textEntity);
    }, $completion);
  }

  @Override
  public Object updateText(final TextEntity textEntity,
      final Continuation<? super Integer> $completion) {
    if (textEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      int _result = 0;
      _result += __updateAdapterOfTextEntity.handle(_connection, textEntity);
      return _result;
    }, $completion);
  }

  @Override
  public Object getTextById(final long textId, final Continuation<? super TextEntity> $completion) {
    final String _sql = "SELECT * FROM texts WHERE text_id = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, textId);
        final int _columnIndexOfTextId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "text_id");
        final int _columnIndexOfTextPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "text_path");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final TextEntity _result;
        if (_stmt.step()) {
          final long _tmpText_id;
          _tmpText_id = _stmt.getLong(_columnIndexOfTextId);
          final String _tmpText_path;
          if (_stmt.isNull(_columnIndexOfTextPath)) {
            _tmpText_path = null;
          } else {
            _tmpText_path = _stmt.getText(_columnIndexOfTextPath);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _result = new TextEntity(_tmpText_id,_tmpText_path,_tmpCreated_at);
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
  public Object getAllTexts(final Continuation<? super List<TextEntity>> $completion) {
    final String _sql = "SELECT * FROM texts";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfTextId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "text_id");
        final int _columnIndexOfTextPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "text_path");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final List<TextEntity> _result = new ArrayList<TextEntity>();
        while (_stmt.step()) {
          final TextEntity _item;
          final long _tmpText_id;
          _tmpText_id = _stmt.getLong(_columnIndexOfTextId);
          final String _tmpText_path;
          if (_stmt.isNull(_columnIndexOfTextPath)) {
            _tmpText_path = null;
          } else {
            _tmpText_path = _stmt.getText(_columnIndexOfTextPath);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _item = new TextEntity(_tmpText_id,_tmpText_path,_tmpCreated_at);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteText(final long textId, final Continuation<? super Integer> $completion) {
    final String _sql = "DELETE FROM texts WHERE text_id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, textId);
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
