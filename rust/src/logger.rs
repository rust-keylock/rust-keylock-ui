use log4rs;
use log4rs::append::file::FileAppender;
use log4rs::config::{Appender, Config, Root, Logger};
use log4rs::encode::pattern::PatternEncoder;
use log::LevelFilter;
use std::env;

pub fn init() -> ::errors::Result<()> {
    let mut logdir = env::home_dir().unwrap_or(env::current_dir()?);
    logdir.push(".rust-keylock/logs/rust-keylock.log");

    let filelogger = FileAppender::builder()
        .encoder(Box::new(PatternEncoder::new("[{d}][{h({l})}][{M}] - {m}{n}")))
        .build(logdir.to_str().unwrap_or("rust-keylock.log"))?;

    let config = Config::builder()
        .appender(Appender::builder().build("filelogger", Box::new(filelogger)))
        .logger(Logger::builder()
            .appender("filelogger")
            .build("hyper", LevelFilter::Error))
        .logger(Logger::builder()
            .appender("filelogger")
            .build("tokio_core", LevelFilter::Error))
        .logger(Logger::builder()
            .appender("filelogger")
            .build("tokio_reactor", LevelFilter::Error))
        .build(Root::builder().appender("filelogger").build(LevelFilter::Debug))?;

    let _handle = log4rs::init_config(config)?;
    Ok(())
}
