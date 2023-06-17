/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package server.database;

import commons.Card;
import commons.Status;
import commons.TaskList;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"checkstyle:MissingJavadocMethod", "checkstyle:javadoctype"})
public class CardRepositoryTest implements CardRepository {
    public final List<Card> cards = new ArrayList<>();
    public final List<String> calledMethods = new ArrayList<>();

    private void call(String name) {
        calledMethods.add(name);
    }


    @Override
    public List<Card> findCardsByTaskListId(Long id) {
        call("findCardsByTaskListId");
        return cards.stream().filter(q -> q.id.equals(id)).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Card> findCardsByTaskListIdAndStatus(Long id, String status) {
        call("findCardsByTaskListIdAndStatus");
        List<Card> res = new ArrayList<>();
        for (Card c : cards) {
            if (c.taskList.id.equals(id) && c.status.name().equals(status))
                res.add(c);
        }
        Collections.sort(res);
        return res;
    }

    @Override
    public List<Card> findNotDeletedCardsByTaskListId(Long id) {
        return cards.stream().filter(c -> !c.status.equals(Status.DELETED) && c.taskList.id.equals(id))
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void addTagToCard(Long cid, Long tid) {
        throw new NotImplementedException();
    }

    @Override
    public void removeTagFromCard(Long cid, Long tid) {
        throw new NotImplementedException();
    }

    @Override
    public List<Card> findAll() {
        call("findAll");
        return cards;
    }

    @Override
    public List<Card> findAll(Sort sort) {
        throw new NotImplementedException();
    }

    @Override
    public Page<Card> findAll(Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public List<Card> findAllById(Iterable<Long> longs) {
        List<Card> list = new ArrayList<>();
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
        return cards.size();
    }

    @Override
    public void deleteById(Long id) {
        call("deleteById");
        Card c = getById(id);
        cards.remove(c);
    }

    @Override
    public void delete(Card entity) {
        call("delete");
        cards.remove(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAll(Iterable<? extends Card> entities) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAll() {
        call("deleteAll");
        cards.removeIf(n -> true);
    }

    @Override
    public <S extends Card> S save(S entity) {
        call("save");
        if (entity.id != null) {
            deleteById(entity.id);
        } else {
            entity.id = (long) cards.size();
        }
        cards.add(entity);
        return entity;
    }

    @Override
    public <S extends Card> List<S> saveAll(Iterable<S> entities) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<Card> findById(Long id) {
        call("findById");
        return find(id);
    }

    @Override
    public boolean existsById(Long id) {
        call("existsById");
        return find(id).isPresent();
    }

    @Override
    public void flush() {
        call("flush");
        // there is nothing to do here
    }

    @Override
    public <S extends Card> S saveAndFlush(S entity) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends Card> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAllInBatch(Iterable<Card> entities) {
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
    public Card getOne(Long id) {
        throw new NotImplementedException();
    }

    @Override
    public Card getById(Long id) {
        call("getById");
        Optional<Card> c = find(id);
        return c.orElse(null);
    }

    public Optional<Card> find(long id) {
        call("find");
        return cards.stream().filter(q -> q.id == id).findFirst();
    }

    @Override
    public <S extends Card> Optional<S> findOne(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends Card> List<S> findAll(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends Card> List<S> findAll(Example<S> example, Sort sort) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends Card> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends Card> long count(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends Card> boolean exists(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends Card, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new NotImplementedException();
    }
}
