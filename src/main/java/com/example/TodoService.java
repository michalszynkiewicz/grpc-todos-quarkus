package com.example;

import com.example.todos.MutinyTodosGrpc;
import com.example.todos.TodosOuterClass;
import com.example.todos.TodosOuterClass.Todo;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@GrpcService
public class TodoService extends MutinyTodosGrpc.TodosImplBase {
    private static final TodosOuterClass.Void VOID = TodosOuterClass.Void.newBuilder().getDefaultInstanceForType();

    private Map<Integer, Todo> todos = new ConcurrentHashMap<>();

    private AtomicInteger idSequence = new AtomicInteger(0);

    private final BroadcastProcessor<Todo> broadcast = BroadcastProcessor.create();

    @Override
    public Uni<TodosOuterClass.Void> add(Todo request) {
        int id = idSequence.getAndIncrement();
        Todo todo = Todo.newBuilder(request).setId(id).build();
        todos.put(id, todo);
        broadcast.onNext(todo);
        return Uni.createFrom().item(VOID);
    }

    @Override
    public Multi<Todo> watch(TodosOuterClass.Void request) {
        return Multi.createBy().merging().streams(Multi.createFrom().iterable(todos.values()), broadcast);
    }

    @Override
    public Uni<TodosOuterClass.Void> markDone(Todo request) {
        Todo todo = Todo.newBuilder(request).setTodoState(TodosOuterClass.State.DONE).build();
        todos.put(request.getId(), todo);
        broadcast.onNext(todo);
        return Uni.createFrom().item(VOID);
    }
}
