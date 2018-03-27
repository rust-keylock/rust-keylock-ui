use log::{self, LogRecord, LogLevel, LogMetadata, LogLevelFilter};
use std::fs::OpenOptions;

pub struct AndroidLogger {}

impl log::Log for AndroidLogger {
    fn enabled(&self, metadata: &LogMetadata) -> bool {
        metadata.level() <= LogLevel::Debug
    }

    fn log(&self, record: &LogRecord) {
        if self.enabled(record.metadata()) {
            let message = format!("{:?}", record.args());
            println!("{}", message);
//            (self.log_cb)(super::to_java_string(record.level().to_string()),
//            	super::to_java_string(record.location().module_path().to_string()),
//            	super::to_java_string(record.location().file().to_string()),
//            	record.location().line() as i32,
//            	super::to_java_string(message))
        }
    }
}

pub fn init() -> ::errors::Result<()>  {
//	let log_path = "rust-keylock.log";
//    let file = OpenOptions::new()
//        .create(true)
//        .write(true)
//        .truncate(false)
//        .open(log_path)?;


    Ok(())
}

