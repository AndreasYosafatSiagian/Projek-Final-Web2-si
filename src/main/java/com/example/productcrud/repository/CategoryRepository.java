package com.example.productcrud.repository;

import com.example.productcrud.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Ambil semua category milik user tertentu
    List<Category> findByUserId(Long userId);

    // Cari category by id DAN userId (mencegah user akses category orang lain)
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    // Cek apakah nama category sudah ada untuk user tersebut (validasi duplikat)
    boolean existsByNameAndUserId(String name, Long userId);

    // Cek duplikat nama tapi exclude id tertentu (dipakai saat edit)
    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);
}