package ro.cs.tao.user;

public interface UserModelAdapter <T> {
    User toTaoUser(T object);
    T fromTaoUser(User user);
}
