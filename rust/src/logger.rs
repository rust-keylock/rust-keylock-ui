// Copyright 2017 astonbitecode
// This file is part of rust-keylock password manager.
//
// rust-keylock is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rust-keylock is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock.  If not, see <http://www.gnu.org/licenses/>.
use std::{env, fs};

use dirs;
use log4rs;
use log4rs::append::rolling_file::policy::compound::CompoundPolicy;
use log4rs::append::rolling_file::policy::compound::roll::fixed_window::FixedWindowRoller;
use log4rs::append::rolling_file::policy::compound::trigger::size::SizeTrigger;
use log4rs::append::rolling_file::RollingFileAppender;
use log4rs::config::{Appender, Config, Logger, Root};
use log4rs::encode::pattern::PatternEncoder;
use log::LevelFilter;

pub fn init() -> crate::errors::Result<()> {
    let mut logdir = dirs::home_dir().unwrap_or(env::current_dir()?);
    logdir.push(".rust-keylock/logs/");
    fs::create_dir_all(&logdir)?;

    logdir.push("rust-keylock.log.{}");
    let rollfile_pattern = logdir.to_str().unwrap_or("rust-keylock.{}.log");
    let policy = Box::new(CompoundPolicy::new(
        // 3 MB
        Box::new(SizeTrigger::new(3_145_728)),
        // 10 files window
        Box::new(FixedWindowRoller::builder().build(rollfile_pattern, 10)?),
    ));

    logdir.pop();
    logdir.push("rust-keylock.log");

    let filelogger = RollingFileAppender::builder()
        .encoder(Box::new(PatternEncoder::new("[{d}][{h({l})}][{M}] - {m}{n}")))
        .build(logdir.to_str().unwrap_or("rust-keylock.log"), policy)?;

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
        .logger(Logger::builder()
            .appender("filelogger")
            .build("j4rs", LevelFilter::Error))
        .build(Root::builder().appender("filelogger").build(LevelFilter::Debug))?;

    let _handle = log4rs::init_config(config)?;
    Ok(())
}
