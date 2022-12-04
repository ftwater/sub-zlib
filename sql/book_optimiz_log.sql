create table if not exists zlib.book_optimize_log
(
    zlibrary_id int not null,
    delete_flag int null,
    constraint book_optimize_log_pk
        primary key (zlibrary_id)
);