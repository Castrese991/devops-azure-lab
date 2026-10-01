package it.castrese.lab.order;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {}
