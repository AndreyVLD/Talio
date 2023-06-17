package server.database;

import commons.TaskList;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"checkstyle:MissingJavadocMethod", "checkstyle:javadoctype"})
public class ListRepositoryTest implements ListRepository {
    public final List<TaskList> lists = new ArrayList<>();
    public final List<String> calledMethods = new ArrayList<>();

    private void call(String name) {
        calledMethods.add(name);
    }

    @Override
    public List<TaskList> findByBoard_Id(long boardId) {
        call("findByBoard_Id");
        return lists.stream().filter(l -> l.board.id.equals(boardId)).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<TaskList> findListsByBoardIdAndStatus(Long id, String status) {
        return null;
    }

    @Override
    public List<TaskList> findNotDeletedListsByBoardId(Long id) {
        return null;
    }

    @Override
    public List<TaskList> findAll() {
        call("findAll");
        return lists;
    }

    @Override
    public List<TaskList> findAll(Sort sort) {
        throw new NotImplementedException();
    }

    @Override
    public Page<TaskList> findAll(Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public List<TaskList> findAllById(Iterable<Long> longs) {
        List<TaskList> list = new ArrayList<>();
        Iterator<Long> iter = longs.iterator();
        while (iter.hasNext()) {
            list.add(getById(iter.next()));
        }
        if (list.size() == 0) return null;
        else return list;
    }

    @Override
    public long count() {
        call("count");
        return lists.size();
    }

    @Override
    public void deleteById(Long id) {
        call("deleteById");
        TaskList taskList = getById(id);
        lists.remove(taskList);
    }

    @Override
    public void delete(TaskList entity) {
        call("delete");
        lists.remove(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAll(Iterable<? extends TaskList> entities) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAll() {
        call("deleteAll");
        lists.removeIf(n -> true);
    }

    @Override
    public <S extends TaskList> S save(S entity) {
        call("save");
        if (entity.id != null) {
            deleteById(entity.id);
        } else {
            entity.id = (long) lists.size();
        }
        lists.add(entity);
        return entity;
    }

    @Override
    public <S extends TaskList> List<S> saveAll(Iterable<S> entities) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<TaskList> findById(Long id) {
        return lists.stream().filter(l -> l.id.equals(id)).findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public void flush() {
        call("flush");
        // nothing to do here
    }

    @Override
    public <S extends TaskList> S saveAndFlush(S entity) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends TaskList> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAllInBatch(Iterable<TaskList> entities) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAllInBatch() {
        throw new NotImplementedException();
    }

    @Override
    public TaskList getOne(Long aLong) {
        throw new NotImplementedException();
    }

    @Override
    public TaskList getById(Long id) {
        call("getById");
        return findById(id).orElse(null);
    }

    @Override
    public <S extends TaskList> Optional<S> findOne(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends TaskList> List<S> findAll(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends TaskList> List<S> findAll(Example<S> example, Sort sort) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends TaskList> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends TaskList> long count(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends TaskList> boolean exists(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends TaskList, R> R findBy(Example<S> example,
                                            Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new NotImplementedException();
    }
}