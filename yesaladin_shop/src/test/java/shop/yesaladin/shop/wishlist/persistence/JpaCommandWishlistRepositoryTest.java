package shop.yesaladin.shop.wishlist.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import shop.yesaladin.shop.file.domain.model.File;
import shop.yesaladin.shop.member.domain.model.Member;
import shop.yesaladin.shop.member.domain.model.MemberGenderCode;
import shop.yesaladin.shop.member.domain.model.MemberGrade;
import shop.yesaladin.shop.product.domain.model.Product;
import shop.yesaladin.shop.product.domain.model.SubscribeProduct;
import shop.yesaladin.shop.product.domain.model.TotalDiscountRate;
import shop.yesaladin.shop.product.dummy.DummyFile;
import shop.yesaladin.shop.product.dummy.DummyProduct;
import shop.yesaladin.shop.product.dummy.DummySubscribeProduct;
import shop.yesaladin.shop.product.dummy.DummyTotalDiscountRate;
import shop.yesaladin.shop.wishlist.domain.model.Wishlist;

@DataJpaTest
@ActiveProfiles("local-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class JpaCommandWishlistRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private JpaCommandWishlistRepository repository;

    private Product product;
    private SubscribeProduct subscribeProduct;
    private File thumbnailFile;
    private File ebookFile;
    private TotalDiscountRate totalDiscountRate;
    private Member member;

    @BeforeEach
    void setUp() {
        subscribeProduct = DummySubscribeProduct.dummy();
        thumbnailFile = DummyFile.dummy("file1/image.png");
        ebookFile = DummyFile.dummy("file2/ebook.pdf");
        totalDiscountRate = DummyTotalDiscountRate.dummy();

        entityManager.persist(subscribeProduct);
        entityManager.persist(thumbnailFile);
        entityManager.persist(ebookFile);
        entityManager.persist(totalDiscountRate);

        product = DummyProduct.dummy(
                "ISBN",
                subscribeProduct,
                thumbnailFile,
                ebookFile,
                totalDiscountRate
        );
        member = Member.builder()
                .nickname("익명의 오소리")
                .name("김홍대")
                .loginId("mongmeo")
                .password("1234")
                .birthYear(2000)
                .birthMonth(1)
                .birthDay(1)
                .email("mongemo@yesaladin.shop")
                .phone("01012345678")
                .signUpDate(LocalDate.now())
                .withdrawalDate(null)
                .isWithdrawal(false)
                .isBlocked(false)
                .blockedReason("")
                .blockedDate(LocalDate.now())
                .unblockedDate(LocalDate.now())
                .memberGrade(MemberGrade.WHITE)
                .memberGenderCode(MemberGenderCode.MALE)
                .build();
    }

    @Test
    @DisplayName("save 성공")
    void save() {
        //given
        entityManager.persist(product);
        entityManager.persist(member);

        Wishlist wishlist = Wishlist.create(member, product);

        //when
        Wishlist result = repository.save(wishlist);

        //then
        assertThat(result.getMember().getId()).isEqualTo(member.getId());
        assertThat(result.getProduct().getId()).isEqualTo(product.getId());
    }
}