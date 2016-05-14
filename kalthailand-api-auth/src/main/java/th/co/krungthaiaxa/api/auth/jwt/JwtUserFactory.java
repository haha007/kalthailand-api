package th.co.krungthaiaxa.api.auth.jwt;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUser create(String userName) {
        return new JwtUser(
                userName,
                null,
                null,
                null
        );
    }
//
//    private static List<GrantedAuthority> mapToGrantedAuthorities(List<Authority> authorities) {
//        return authorities.stream()
//                .map(authority -> new SimpleGrantedAuthority(authority.getName().name()))
//                .collect(Collectors.toList());
//    }
}
