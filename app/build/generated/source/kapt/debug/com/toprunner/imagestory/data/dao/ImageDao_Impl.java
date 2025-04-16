package com.toprunner.imagestory.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteConnectionUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.toprunner.imagestory.data.entity.ImageEntity;
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
public final class ImageDao_Impl implements ImageDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<ImageEntity> __insertAdapterOfImageEntity;

  private final EntityDeleteOrUpdateAdapter<ImageEntity> __updateAdapterOfImageEntity;

  public ImageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfImageEntity = new EntityInsertAdapter<ImageEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `images` (`image_id`,`title`,`image_path`,`created_at`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final ImageEntity entity) {
        statement.bindLong(1, entity.getImage_id());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        if (entity.getImage_path() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getImage_path());
        }
        statement.bindLong(4, entity.getCreated_at());
      }
    };
    this.__updateAdapterOfImageEntity = new EntityDeleteOrUpdateAdapter<ImageEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `images` SET `image_id` = ?,`title` = ?,`image_path` = ?,`created_at` = ? WHERE `image_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final ImageEntity entity) {
        statement.bindLong(1, entity.getImage_id());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        if (entity.getImage_path() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getImage_path());
        }
        statement.bindLong(4, entity.getCreated_at());
        statement.bindLong(5, entity.getImage_id());
      }
    };
  }

  @Override
  public Object insertImage(final ImageEntity imageEntity,
      final Continuation<? super Long> $completion) {
    if (imageEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfImageEntity.insertAndReturnId(_connection, imageEntity);
    }, $completion);
  }

  @Override
  public Object updateImage(final ImageEntity imageEntity,
      final Continuation<? super Integer> $completion) {
    if (imageEntity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      int _result = 0;
      _result += __updateAdapterOfImageEntity.handle(_connection, imageEntity);
      return _result;
    }, $completion);
  }

  @Override
  public Object getImageById(final long imageId,
      final Continuation<? super ImageEntity> $completion) {
    final String _sql = "SELECT * FROM images WHERE image_id = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, imageId);
        final int _columnIndexOfImageId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "image_id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfImagePath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "image_path");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final ImageEntity _result;
        if (_stmt.step()) {
          final long _tmpImage_id;
          _tmpImage_id = _stmt.getLong(_columnIndexOfImageId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpImage_path;
          if (_stmt.isNull(_columnIndexOfImagePath)) {
            _tmpImage_path = null;
          } else {
            _tmpImage_path = _stmt.getText(_columnIndexOfImagePath);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _result = new ImageEntity(_tmpImage_id,_tmpTitle,_tmpImage_path,_tmpCreated_at);
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
  public Object getAllImages(final Continuation<? super List<ImageEntity>> $completion) {
    final String _sql = "SELECT * FROM images";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfImageId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "image_id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfImagePath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "image_path");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final List<ImageEntity> _result = new ArrayList<ImageEntity>();
        while (_stmt.step()) {
          final ImageEntity _item;
          final long _tmpImage_id;
          _tmpImage_id = _stmt.getLong(_columnIndexOfImageId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpImage_path;
          if (_stmt.isNull(_columnIndexOfImagePath)) {
            _tmpImage_path = null;
          } else {
            _tmpImage_path = _stmt.getText(_columnIndexOfImagePath);
          }
          final long _tmpCreated_at;
          _tmpCreated_at = _stmt.getLong(_columnIndexOfCreatedAt);
          _item = new ImageEntity(_tmpImage_id,_tmpTitle,_tmpImage_path,_tmpCreated_at);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteImage(final long imageId, final Continuation<? super Integer> $completion) {
    final String _sql = "DELETE FROM images WHERE image_id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, imageId);
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
